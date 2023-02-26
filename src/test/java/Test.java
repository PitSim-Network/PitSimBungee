import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Test {
	public static DiscordOAuth oauthHandler;
	public static final String CLIENT_ID = "841567626466951171";
	public static final String OAUTH_SECRET = "L-X8MhiQ8Gi2H7wgLm3-FBjBfrSxrOql";

	public static void main(String[] args) {
		oauthHandler = new DiscordOAuth(CLIENT_ID, OAUTH_SECRET,
				"http://localhost:3000", new String[] {"identify", "guilds.join"});

		new Thread(() -> {
			try(ServerSocket serverSocket = new ServerSocket(3000)) {
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
				System.out.println("Received a connection");

				// Get input and output streams
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream());

				while(true) {
					String line = in.readLine();
					if(line == null || line.isEmpty()) break;
					System.out.println(line);

					String[] parts = line.split(" ");
					String requestUrl = parts[1];
					String[] queryParams = requestUrl.split("\\?");
					if(queryParams.length <= 1) continue;
					String queryString = queryParams[1];
					String[] params = queryString.split("&");

					String code = null;
					String state = null;
					for(String param : params) {
						String[] keyValue = param.split("=");
						if(keyValue.length <= 1) continue;
						String key = keyValue[0];
						String value = keyValue[1];
						if(key.equals("code")) {
							code = value;
						} else if(key.equals("state")) {
							state = value;
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

				// Write out our header to the client
//			out.println("Echo Server 1.0");
//			out.flush();

				// Echo lines back to the client until the client closes the connection or we receive an empty line
//			String line = in.readLine();
//			while(line != null && line.length() > 0) {
//				out.println("Echo: " + line);
//				out.flush();
//				line = in.readLine();
//			}

				// Close our connection
				in.close();
				out.close();
				socket.close();

				System.out.println("Connection closed");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void authenticate(String code, String state) {
			System.out.println("Code: " + code + " State: " + state);
			try {
				TokensResponse tokens = Test.oauthHandler.getTokens(code);
				System.out.println(tokens.getExpiresIn());
				String accessToken = tokens.getAccessToken();
				String refreshToken = tokens.getRefreshToken();
				System.out.println(accessToken);
				System.out.println(refreshToken);

				DiscordAPI api = new DiscordAPI(accessToken);
				User user = api.fetchUser();
				System.out.println(user.getFullUsername() + " " + user.getUsername() + " " + user.getId());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}