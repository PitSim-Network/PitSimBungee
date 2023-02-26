package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.IdentificationManager;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class AuthenticationManager implements Listener {
	public static DiscordOAuth oauthHandler;
	public static final String CLIENT_ID = "841567626466951171";
	public static final String OAUTH_SECRET = "L-X8MhiQ8Gi2H7wgLm3-FBjBfrSxrOql";

	public static Map<UUID, UUID> secretClientStateMap = new HashMap<>();
	public static List<UUID> rewardVerificationList = new ArrayList<>(); // players who weren't on a pitsim server when they verified

	static {
		oauthHandler = new DiscordOAuth(CLIENT_ID, OAUTH_SECRET,
				"http://51.81.48.25:3000", new String[] {"identify", "guilds.join"});

		new Thread(() -> {
			try(ServerSocket serverSocket = new ServerSocket(3000)) {
				System.out.println("listening on port 3000");
				while(true) {
					Socket socket = serverSocket.accept();
					RequestHandler requestHandler = new RequestHandler(socket);
					requestHandler.start();
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}).start();
	}

	public static class RequestHandler extends Thread {
		private final Socket socket;

		RequestHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				System.out.println("receiving a connection on port 3000");

				// Get input and output streams
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream());

				while(true) {
					String line = in.readLine();
					if(line == null || line.isEmpty()) break;

					String[] parts = line.split(" ");
					String requestUrl = parts[1];
					String[] queryParams = requestUrl.split("\\?");
					if(queryParams.length <= 1) continue;
					String queryString = queryParams[1];
					String[] params = queryString.split("&");

					String code = null;
					UUID state = null;
					for(String param : params) {
						String[] keyValue = param.split("=");
						if(keyValue.length <= 1) continue;
						String key = keyValue[0];
						String value = keyValue[1];
						if(key.equals("code")) {
							code = value;
						} else if(key.equals("state")) {
							state = UUID.fromString(value);
						}
					}
					if(code != null && state != null) {
						authenticate(code, state);
						break;
					}
				}

				String response = "HTTP/1.1 302 Found\r\n" +
						"Location: https://discord.com/invite/XtrHEXcHHr\r\n" +
						"Connection: close\r\n\r\n";
				out.print(response);
				out.flush();

				in.close();
				out.close();
				socket.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void authenticate(String code, UUID state) {
			try {
				TokensResponse tokens = oauthHandler.getTokens(code);
				String accessToken = tokens.getAccessToken();
				String refreshToken = tokens.getRefreshToken();
				System.out.println(accessToken);
				System.out.println(refreshToken);

				DiscordAPI api = new DiscordAPI(accessToken);
				User user = api.fetchUser();
				long userId = Long.parseLong(user.getId());

				UUID playerUUID = secretClientStateMap.get(state);
				ProxiedPlayer proxiedPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);

				DiscordUser previousUser = DiscordManager.getUser(userId);
				if(previousUser != null) {
					try {
						AOutput.error(proxiedPlayer, "&c&lERROR!&7 Your discord (" + user.getFullUsername() +
								") is already linked to " + IdentificationManager.getUsername(
										IdentificationManager.getConnection(), previousUser.uuid));
					} catch(SQLException e) {
						throw new RuntimeException(e);
					}
					return;
				}

				DiscordUser discordUser = new DiscordUser(playerUUID, userId, accessToken, refreshToken);
				System.out.println(user.getFullUsername() + " " + user.getUsername() + " " + user.getId());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void attemptAuthentication(ProxiedPlayer proxiedPlayer) {
		DiscordUser discordUser = DiscordManager.getUser(proxiedPlayer.getUniqueId());

		if(discordUser != null && discordUser.isAuthenticated()) {
			AOutput.error(proxiedPlayer, "&c&lERROR!&7 You are already authenticated");
			return;
		}

		secretClientStateMap.putIfAbsent(proxiedPlayer.getUniqueId(), UUID.randomUUID());
		UUID clientState = secretClientStateMap.get(proxiedPlayer.getUniqueId());

		boolean isOnlinePitSim = false;
		PluginMessage pluginMessage = new PluginMessage()
				.writeString("AUTH_SUCCESS")
				.writeString(proxiedPlayer.toString());
		for(MainGamemodeServer server : MainGamemodeServer.serverList) {
			if(!server.status.isOnline() || server.getServerInfo() != proxiedPlayer.getServer().getInfo()) continue;
			pluginMessage.addServer(server.getServerInfo());
			isOnlinePitSim = true;
			break;
		}
		pluginMessage.send();
		if(!isOnlinePitSim) rewardVerificationList.add(proxiedPlayer.getUniqueId());

		sendAuthenticationLink(proxiedPlayer, clientState);
	}

	public static void sendAuthenticationLink(ProxiedPlayer proxiedPlayer, UUID clientState) {
		TextComponent text = new TextComponent(ChatColor.translateAlternateColorCodes('&',
				"&9&lLINK!&7 Click me to link your discord account"));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/api/oauth2/authorize?" +
				"client_id=841567626466951171&redirect_uri=http%3A%2F%2F51.81.48.25%3A3000&response_type=code&" +
				"scope=identify%20guilds.join&state=" + clientState.toString()));
		proxiedPlayer.sendMessage(text);
	}
}
