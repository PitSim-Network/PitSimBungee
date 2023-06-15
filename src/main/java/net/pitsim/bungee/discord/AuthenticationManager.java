package net.pitsim.bungee.discord;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import org.bukkit.ChatColor;
import org.jsoup.HttpStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AuthenticationManager implements Listener {
	public static DiscordOAuth oauthHandler;
	public static final String CLIENT_ID = "841567626466951171";
	public static final String OAUTH_SECRET = "L-X8MhiQ8Gi2H7wgLm3-FBjBfrSxrOql";

	public static Map<UUID, UUID> secretClientStateMap = new HashMap<>();
	public static List<UUID> rewardVerificationList = new ArrayList<>(); // players who weren't on a pitsim server when they verified
	public static Map<UUID, User> recentlyAuthenticatedUserMap = new HashMap<>();

	public static List<UUID> queuedUsers = new ArrayList<>();

	static {
		if(!ConfigManager.isDev()) {
			oauthHandler = new DiscordOAuth(CLIENT_ID, OAUTH_SECRET,
					"http://147.135.8.130:3000", new String[] {"identify", "guilds.join"});

			new Thread(() -> {
				try(ServerSocket serverSocket = new ServerSocket(3000)) {
					System.out.println("listening for discord authentications on port 3000");
					while(true) {
						Socket socket = serverSocket.accept();
						RequestHandler requestHandler = new RequestHandler(socket);
						requestHandler.start();
					}
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}).start();

			((ProxyRunnable) () -> {
				if(queuedUsers.isEmpty()) queuedUsers = DiscordManager.getAllDiscordUserUUIDs();
				if(queuedUsers.isEmpty()) return;
				UUID uuid = queuedUsers.remove(0);

				DiscordUser user = DiscordManager.getUser(uuid);
				if(user == null || user.lastRefresh + 1000 * 60 * 60 * 24 > System.currentTimeMillis() || user.accessToken.equals("INVALID")) return;

				try {
					TokensResponse tokens = oauthHandler.refreshTokens(user.refreshToken);
					user.accessToken = tokens.getAccessToken();
					user.refreshToken = tokens.getRefreshToken();
					user.lastRefresh = System.currentTimeMillis();
					user.save();
				} catch(HttpStatusException exception) {
					int statusCode = exception.getStatusCode();
					String name = BungeeMain.getName(uuid, false);
					if(statusCode == 400) {
						System.out.println("Marking discord user as invalid: " + name);
						user.accessToken = "INVALID";
						user.refreshToken = "INVALID";
						user.save();
					}
				} catch(IOException exception) {
					exception.printStackTrace();
					queuedUsers.add(uuid);
				}
			}).runAfterEvery(1, 1, TimeUnit.MINUTES);
		}
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
					if(parts.length <= 1) continue;
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
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}

		public void authenticate(String code, UUID state) {
			try {
				TokensResponse tokens = oauthHandler.getTokens(code);
				String accessToken = tokens.getAccessToken();
				String refreshToken = tokens.getRefreshToken();

				DiscordAPI api = new DiscordAPI(accessToken);
				User user = api.fetchUser();
				long userId = Long.parseLong(user.getId());

				UUID playerUUID = null;
				for(Map.Entry<UUID, UUID> entry : secretClientStateMap.entrySet()) {
					if(!entry.getValue().equals(state)) continue;
					playerUUID = entry.getKey();
					break;
				}
				if(playerUUID == null) return;
				ProxiedPlayer proxiedPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);

				DiscordUser previousUser = DiscordManager.getUser(userId);
				if(previousUser != null) {
					AOutput.error(proxiedPlayer, "&c&lERROR!&7 Your discord (" + user.getFullUsername() +
							") is already linked to " + BungeeMain.getName(previousUser.uuid, false));
					return;
				}

				DiscordUser discordUser = new DiscordUser(playerUUID, userId, accessToken, refreshToken);
				discordUser.save();
				try {
					discordUser.joinDiscord();
				} catch(Exception ignored) {}

				boolean isOnlinePitSim = false;
				for(PitSimServer server : PitSimServerManager.mixedServerList) {
					if(!server.status.isOnline() || server.getServerInfo() != proxiedPlayer.getServer().getInfo()) continue;
					isOnlinePitSim = true;
					break;
				}
				AOutput.color(proxiedPlayer, "&9&lLINK!&7 You have successfully been linked to " + user.getFullUsername());
				if(isOnlinePitSim) {
					rewardPlayer(proxiedPlayer);
				} else {
					rewardVerificationList.add(proxiedPlayer.getUniqueId());
				}

				try {
					Objects.requireNonNull(DiscordManager.JDA.getTextChannelById(Constants.VERIFICATION_LOG_CHANNEL))
							.sendMessage("Discord: `" + user.getFullUsername() + "`" +
									"\nIGN/UUID: `" + BungeeMain.getName(playerUUID, false) + "`").queue();
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			} catch(IOException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	public static void attemptAuthentication(ProxiedPlayer proxiedPlayer) {
		DiscordUser discordUser = DiscordManager.getUser(proxiedPlayer.getUniqueId());

		if(discordUser != null && discordUser.isAuthenticated()) {
			User user = recentlyAuthenticatedUserMap.get(proxiedPlayer.getUniqueId());
			AOutput.error(proxiedPlayer, "&c&lERROR!&7 You are already authenticated (" + user.getFullUsername() + ")");
			return;
		}

		secretClientStateMap.putIfAbsent(proxiedPlayer.getUniqueId(), UUID.randomUUID());
		UUID clientState = secretClientStateMap.get(proxiedPlayer.getUniqueId());

		sendAuthenticationLink(proxiedPlayer, clientState);
	}

	public static void sendAuthenticationLink(ProxiedPlayer proxiedPlayer, UUID clientState) {
		TextComponent text = new TextComponent(ChatColor.translateAlternateColorCodes('&',
				"&9&lLINK!&7 Click me to link your discord account"));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/api/oauth2/authorize?" +
				"client_id=841567626466951171&redirect_uri=http%3A%2F%2F147.135.8.130%3A3000&response_type=code&" +
				"scope=identify%20guilds.join&state=" + clientState.toString()));
		proxiedPlayer.sendMessage(text);
	}

	public static void rewardPlayer(ProxiedPlayer proxiedPlayer) {
		new PluginMessage()
				.writeString("AUTH_SUCCESS")
				.writeString(proxiedPlayer.getUniqueId().toString())
				.addServer(proxiedPlayer.getServer().getInfo())
				.send();
	}
}
