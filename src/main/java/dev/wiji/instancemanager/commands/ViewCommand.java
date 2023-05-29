package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.guilds.GuildMessaging;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewCommand extends Command {
	public static final long COOLDOWN_MS = 1000 * 3;
	public static Map<UUID, Long> cooldownMap = new HashMap<>();

	public ViewCommand() {
		super("view");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(!PitSimServerManager.isInPitSim(player)) {
			AOutput.error(player, "&cYou are not in a PitSim server");
			return;
		}

		if(args.length != 1) {
			AOutput.error(player, "&cUsage: /view <player>");
			return;
		}

		String targetString = args[0];


		UUID targetUUID = BungeeMain.getUUID(targetString, false);
		if(targetUUID == null) {
			AOutput.error(player, "&cCould not find that player");
			return;
		}

		if(cooldownMap.getOrDefault(player.getUniqueId(), 0L) + COOLDOWN_MS > System.currentTimeMillis()) {
			AOutput.error(player, "&c&lERROR! &7Please wait before using this command!");
			return;
		}

		sendViewData(player, targetUUID);
		cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public static void sendViewData(ProxiedPlayer player, UUID targetUUID) {
		ServerInfo executorServer = player.getServer().getInfo();
		PluginMessage message = new PluginMessage().writeString("VIEW INFO").writeString(targetUUID.toString())
				.writeString(player.getUniqueId().toString());
		message.addServer(executorServer);

		StorageProfile profile = StorageManager.getStorage(targetUUID);
		profile.sendToServer(executorServer, true);

		PitSimServer server = PitSimServer.getServer(executorServer);
		GuildMessaging.sendGuildData(targetUUID, server);
		message.writeString(BungeeMain.getName(targetUUID, false));

		message.send();
	}
}
