package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.util.UUID;

public class MigrateCommand extends Command {
	public MigrateCommand() {
		super("migrate");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		File directory = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
		ServerInfo server = BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-1");

		
		for(File saveFile : directory.listFiles()) {
			UUID playerUUID = UUID.fromString(saveFile.getName().substring(0, saveFile.getName().length() - 4));

			StorageProfile profile = StorageManager.getStorage(playerUUID);

			profile.sendToServer(server, false);
			PluginMessage message = new PluginMessage().writeString("MIGRATE ITEMS").writeString(playerUUID.toString());
			message.addServer(server).send();
		}
	}
}
