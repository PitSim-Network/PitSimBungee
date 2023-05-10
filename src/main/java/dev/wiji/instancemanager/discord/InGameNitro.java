package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class InGameNitro {
	public static final Role nitroRole = DiscordManager.MAIN_GUILD.getRoleById(Constants.NITRO_ROLE_ID);
	public boolean hasCalculatedSkins = false;

	public InGameNitro() {
		((ProxyRunnable) () -> {
			List<Member> members = DiscordManager.MAIN_GUILD.findMembers(member -> member.getRoles().contains(nitroRole)).get();
			Map<UUID, String> memberInfo = new HashMap<>();

			for(Member member : members) {

				UUID playerUUID;
				String effectiveName = member.getEffectiveName().toLowerCase();
				playerUUID = BungeeMain.getUUID(effectiveName, false);
				if(playerUUID == null) continue;
				memberInfo.put(playerUUID, member.getEffectiveName());

//				User luckPermsUser = BungeeMain.LUCKPERMS.getUserManager().getUser(playerUUID);
				UserManager userManager = BungeeMain.LUCKPERMS.getUserManager();
				CompletableFuture<User> userFuture = userManager.loadUser(playerUUID);
				UUID finalPlayerUUID = playerUUID;
				userFuture.thenAccept(user -> {
					if(user == null) {
						return;
					}
					Node node = Node.builder("group.nitro")
							.value(true)
							.expiry(Duration.ofMinutes(4))
							.build();

					for(Node playerNode : user.getNodes()) {
						if(!playerNode.equals(node, NodeEqualityPredicate.ONLY_KEY)) continue;
						try {
							BungeeMain.LUCKPERMS.getUserManager().modifyUser(finalPlayerUUID, modifyUser -> modifyUser.data().remove(playerNode)).get();
						} catch(InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}

					BungeeMain.LUCKPERMS.getUserManager().modifyUser(finalPlayerUUID, modifyUser -> modifyUser.data().add(node));
				});
			}

			try {
				if(!hasCalculatedSkins) {
					sendSkinData(memberInfo);
					hasCalculatedSkins = true;
				}
			} catch(Exception ignored) {}
		}).runAfterEvery(0L, 3 * 10, TimeUnit.SECONDS);
	}

	public void sendSkinData(Map<UUID, String> memberInfo) {
		List<SkinData> skinDataList = new ArrayList<>();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(memberInfo.isEmpty()) {
					cancel();
					return;
				}
				UUID uuid = memberInfo.keySet().iterator().next();
				String memberIGN = memberInfo.get(uuid);

				try {
					SkinData skinData = fetchSkinData(uuid, memberIGN);
					if(skinData != null && skinData.textureSignature != null && skinData.textureValue != null) {
						skinDataList.add(skinData);
					} else {
						System.out.println("Failed to fetch skin data for " + memberIGN);
					}

				} catch(Exception e) {
					throw new RuntimeException(e + "\nFailed to fetch skin data for " + memberIGN);
				}

				memberInfo.remove(uuid);
			}
		}, 0, 1000 * 5);


		((ProxyRunnable) () -> {
			PluginMessage message = new PluginMessage().writeString("NITRO PLAYERS");
			for(SkinData skinData : skinDataList) {
				String stringData = skinData.name + "," + skinData.textureValue + "," + skinData.textureSignature;
				message.writeString(stringData);
			}

			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(!pitSimServer.status.isOnline()) continue;

				message.addServer(pitSimServer.getServerInfo());
			}

			message.send();
		}).runAfterEvery(1, 1, TimeUnit.MINUTES);
	}

	private static final String TEXTURE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/";
	private static SkinData fetchSkinData(UUID uuid, String name) throws Exception {
		URL url = new URL(TEXTURE_ENDPOINT + uuid.toString() + "?unsigned=false");
		String response = sendRequest(url);
		if(response == null) return null;
		String textureValue = response.split("\"value\" : \"")[1].split("\"")[0];
		String textureSignature = response.split("\"signature\" : \"")[1].split("\"")[0];
		return new SkinData(uuid, name, textureValue, textureSignature);
	}

	public static String sendRequest(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			Scanner scanner = new Scanner(connection.getInputStream());
			String response = scanner.useDelimiter("\\A").next();
			scanner.close();
			return response;
		} else {
			System.out.println("Error: " + responseCode + "for URL: " + url);
		}
		return null;
	}

	private static class SkinData {
		public UUID uuid;
		public String name;
		public String textureValue;
		public String textureSignature;

		public SkinData(UUID uuid, String name, String textureValue, String textureSignature) {
			this.uuid = uuid;
			this.name = name;
			this.textureValue = textureValue;
			this.textureSignature = textureSignature;
		}
	}
}
