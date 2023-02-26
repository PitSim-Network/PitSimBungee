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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthenticationManager implements Listener {
	public static DiscordOAuth oauthHandler;
	public static final String CLIENT_ID = "841567626466951171";
	public static final String OAUTH_SECRET = "L-X8MhiQ8Gi2H7wgLm3-FBjBfrSxrOql";

	public static Map<UUID, UUID> secretClientStateMap = new HashMap<>();

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

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();
		List<Long> longs = message.getLongs();
		List<Boolean> booleans = message.getBooleans();
		if(strings.isEmpty()) return;

		if(strings.get(0).equals("AUTH_REQUEST")) {
			String serverName = strings.get(1);
			UUID playerUUID = UUID.fromString(strings.get(2));

			PluginMessage pluginMessage = new PluginMessage()
					.writeString("AUTH_RESPONSE")
					.writeString(playerUUID.toString());

			DiscordUser discordUser = DiscordManager.getUser(playerUUID);

			if(discordUser != null && discordUser.isAuthenticated()) {
				pluginMessage.writeString(AuthStatus.MINECRAFT_ALREADY_AUTHENTICATED.name());
			} else {
				secretClientStateMap.putIfAbsent(playerUUID, UUID.randomUUID());
				UUID clientState = secretClientStateMap.get(playerUUID);

				pluginMessage.writeString(AuthStatus.READY_FOR_AUTHENTICATION.name());
				pluginMessage.writeString(clientState.toString());
			}

			for(MainGamemodeServer server : MainGamemodeServer.serverList) {
				if(!server.status.isOnline() || !server.getServerInfo().getName().equals(serverName)) continue;
				pluginMessage.addServer(server.getServerInfo());
				break;
			}
			pluginMessage.send();
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

				DiscordUser previousUser = DiscordManager.getUser();
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

				DiscordUser discordUser = new DiscordUser(playerUUID, user.getId())
				System.out.println(user.getFullUsername() + " " + user.getUsername() + " " + user.getId());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public enum AuthStatus {
		DISCORD_ALREADY_AUTHENTICATED,
		MINECRAFT_ALREADY_AUTHENTICATED,
		READY_FOR_AUTHENTICATION
	}
}
