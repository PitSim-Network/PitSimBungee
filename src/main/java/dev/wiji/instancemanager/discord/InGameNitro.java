package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class InGameNitro extends ListenerAdapter {
	public Role nitroRole = DiscordManager.MAIN_GUILD.getRoleById(Constants.NITRO_ROLE_ID);

	public InGameNitro() {
		((ProxyRunnable) () -> {
			List<Member> members = DiscordManager.MAIN_GUILD.findMembers(member -> member.getRoles().contains(nitroRole)).get();
			List<String> memberIGNs = new ArrayList<>();

			for(Member member : members) {

				UUID playerUUID;
				String effectiveName = member.getEffectiveName().toLowerCase();
				playerUUID = BungeeMain.getUUID(effectiveName, false);
				if(playerUUID == null) continue;
				memberIGNs.add(member.getEffectiveName());

				//					User luckPermsUser = DiscordPlugin.LUCKPERMS.getUserManager().getUser(playerUUID);
				UserManager userManager = DiscordPlugin.LUCKPERMS.getUserManager();
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
							DiscordPlugin.LUCKPERMS.getUserManager().modifyUser(finalPlayerUUID, modifyUser -> modifyUser.data().remove(playerNode)).get();
						} catch(InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}

					DiscordPlugin.LUCKPERMS.getUserManager().modifyUser(finalPlayerUUID, modifyUser -> modifyUser.data().add(node));
				});
			}

			try {
				sendNitroMessages(memberIGNs);
			} catch(Exception ignored) {}
		}).runAfterEvery(0L, 3 * 10, TimeUnit.SECONDS);
	}

	public void sendNitroMessages(List<String> memberIGNs) {
		PluginMessage message = new PluginMessage().writeString("NITRO PLAYERS");
		for(String memberIGN : memberIGNs) {
			message.writeString(memberIGN);
		}

		for(OverworldServer overworldServer : OverworldServerManager.serverList) {
			if(!overworldServer.status.isOnline()) continue;

			message.addServer(overworldServer.getServerInfo());
		}

		message.send();
	}

	@Override
	public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
		Member member = event.getMember();
//		event.get
	}
}
