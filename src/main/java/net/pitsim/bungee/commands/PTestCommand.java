package net.pitsim.bungee.commands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.guilds.GuildMessaging;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import net.pitsim.bungee.storage.StorageManager;
import net.pitsim.bungee.storage.StorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.UUID;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
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
