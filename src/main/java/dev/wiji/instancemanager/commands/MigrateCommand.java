package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MigrateCommand extends Command {
	public MigrateCommand() {
		super("migrate");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		File directory = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
		String serverName = ConfigManager.isDev() ? "pitsimdev-1" : "pitsim-1";
		ServerInfo server = BungeeMain.INSTANCE.getProxy().getServerInfo(serverName);

		List<File> files = Arrays.asList(directory.listFiles());
		for(int i = 0; i < files.size(); i++) {
			File saveFile = files.get(i);
			UUID playerUUID = UUID.fromString(saveFile.getName().split("\\.")[0]);

			StorageProfile profile = StorageManager.getStorage(playerUUID);

			profile.sendToServer(server, false);
			PluginMessage message = new PluginMessage().writeString("MIGRATE ITEMS").writeString(playerUUID.toString());
			message.addServer(server).send();
//			AOutput.log("Requesting migration for " + playerUUID + " from server " + serverName);
			if(i % 100 == 0) AOutput.log("Migration Progress: " + (i + 1) + "/" + files.size());
		}
		AOutput.log("Migration Complete");
	}
}
