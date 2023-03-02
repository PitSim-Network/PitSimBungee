package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeploymentManager extends ListenerAdapter {
	public static int thisPort;
	public static String secretHandshake;

	static {
		thisPort = ConfigManager.configuration.getInt("this-socket-port");
		secretHandshake = ConfigManager.configuration.getString("secret-handshake");

		new Thread(() -> {
			try(ServerSocket serverSocket = new ServerSocket(thisPort)) {
				log("listening for deployment requests on port " + thisPort);
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
				log("receiving a connection on port " + thisPort);

				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream());

				List<String> lines = new ArrayList<>();
				while(true) {
					String line = in.readLine();
					if(line == null || line.isEmpty()) break;
					lines.add(line);
					if(lines.size() == 3) break;
				}

				if(lines.size() < 3) {
					log("Invalid amount of lines in request");
					return;
				}

				String requestHandshake = lines.get(0);
				if(!requestHandshake.equals(secretHandshake)) {
					log("Invalid handshake");
					return;
				}

				String serverName = lines.get(1);
				log("Valid handshake received from server: " + serverName);

				String fileName = lines.get(2);
				String fileNameNoExtension = fileName.split("\\.")[0];
				String extension = fileName.split("\\.")[1];

				if(!ConfigManager.configuration.getSection("allowed-downloads").getKeys().contains(fileNameNoExtension) ||
						!extension.equals("jar")) {
					log("Request failed; this file is not on the whitelist");
					return;
				}

				long messageID = ConfigManager.configuration.getSection("allowed-downloads").getLong(fileNameNoExtension);
				long requestedMessageID;
				try {
					requestedMessageID = Long.parseLong(in.readLine());
				} catch(Exception ignored) {
					return;
				}

				File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/autodeploy/" + fileName);

				if(messageID == requestedMessageID) {
					out.println("FAILED");
					out.flush();
					log("Server already has up-to-date file: " + fileName);
				} else if(!file.exists()) {
					out.println("FAILED");
					out.flush();
					log("Cannot find file to deploy: " + fileName);
				} else {
					out.println("SUCCESS");
					out.println(messageID);
					out.flush();

					String path = file.getAbsolutePath();
					byte[] fileBytes = Files.readAllBytes(Paths.get(path));

					log("Sending File (" + fileName + ") to Server: " + serverName);

					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(fileBytes);
					outputStream.flush();

					log("Sent File (" + fileName + ") to Server: " + serverName);
				}

				in.close();
				out.close();
				socket.close();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		if(guild.getIdLong() != Constants.PRIVATE_GUILD_ROLE_ID) return;
		TextChannel textChannel = event.getChannel();
		if(textChannel.getIdLong() != Constants.AUTOMATIC_DEPLOYMENT_CHANNEL) return;
		Member member = event.getMember();
		if(member == null) return;
		Message message = event.getMessage();
		if(message.getAttachments().size() != 1) return;
		Message.Attachment attachment = message.getAttachments().get(0);
		String fileNameNoExtension = attachment.getFileName().split("\\.")[0];
		String extension = attachment.getFileName().split("\\.")[1];

		if(!ConfigManager.configuration.getSection("allowed-downloads").getKeys().contains(fileNameNoExtension) ||
				!extension.equals("jar")) {
			message.reply("Upload failed: jar is not on the whitelist").queue();
			return;
		}

		new File(BungeeMain.INSTANCE.getDataFolder() + "/autodeploy/").mkdirs();
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/autodeploy/" + attachment.getFileName());
		try {
			file.createNewFile();
			attachment.downloadToFile(file).get();
		} catch(InterruptedException | ExecutionException | IOException exception) {
			throw new RuntimeException(exception);
		}

		ConfigManager.configuration.set("allowed-downloads." + fileNameNoExtension, message.getIdLong());
		ConfigManager.save();

		message.reply("Downloaded File: " + attachment.getFileName()).queue();
	}

	public static void log(String message) {
		System.out.println("[AutoDeploy] " + message);
	}
}
