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

import java.util.UUID;

public class ViewCommand extends Command {
	public ViewCommand() {
		super("view");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!player.hasPermission("pitsim.admin")) return;
		if(!PitSimServerManager.isInPitSim(player)) {
			AOutput.error(player, "&cYou are not in a PitSim server");
			return;
		}

		String targetString = args[0];
		ProxiedPlayer target = BungeeMain.INSTANCE.getProxy().getPlayer(targetString);
		ServerInfo executorServer = player.getServer().getInfo();

		UUID targetUUID = BungeeMain.getUUID(targetString, false);
		if(targetUUID == null) {
			AOutput.error(player, "&cCould not find that player");
			return;
		}

		PluginMessage message = new PluginMessage().writeString("VIEW INFO").writeString(targetUUID.toString())
				.writeString(player.getUniqueId().toString());
		message.addServer(executorServer);

		if(target == null || executorServer != target.getServer().getInfo()) {
			message.writeBoolean(false);
			StorageProfile profile = StorageManager.getStorage(targetUUID);
			profile.sendToServer(executorServer, true);

			PitSimServer server = PitSimServer.getServer(executorServer);
			GuildMessaging.sendGuildData(targetUUID, server);
			message.writeString(BungeeMain.getName(targetUUID, false));

		} else message.writeBoolean(true);

		message.send();
	}
}
