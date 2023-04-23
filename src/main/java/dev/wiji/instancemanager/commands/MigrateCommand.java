package dev.wiji.instancemanager.commands;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.storage.OldStorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MigrateCommand extends Command {

	public MigrateCommand() {
		super("migrate");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) return;

		File directory = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
		List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
		int count = 0;
		for(File file : files) {
			OldStorageProfile oldStorageProfile;
			try {
				Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
				oldStorageProfile = new Gson().fromJson(reader, OldStorageProfile.class);
			} catch(Exception exception) {
				exception.printStackTrace();
				return;
			}
			oldStorageProfile.saveFile = file;
			oldStorageProfile.save();
			System.out.println(++count + "/" + files.size());
		}

//		File directory = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
//		String serverName = ConfigManager.isDev() ? "pitsimdev-1" : "pitsim-1";
//		ServerInfo server = BungeeMain.INSTANCE.getProxy().getServerInfo(serverName);
//
//		List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
//		new Thread(() -> {
//			while(!files.isEmpty()) {
////				if(!readyForNextPlayer) {
////					sleep(1);
////					continue;
////				}
//				File saveFile = files.remove(0);
//				UUID playerUUID = UUID.fromString(saveFile.getName().split("\\.")[0]);
//
//				StorageProfile profile = StorageManager.getStorage(playerUUID);
//
//				profile.sendToServer(server, false);
//				PluginMessage message = new PluginMessage().writeString("MIGRATE ITEMS").writeString(playerUUID.toString());
//				message.addServer(server).send();
//				readyForNextPlayer = false;
////				AOutput.log("Requesting migration for " + playerUUID + " from server " + serverName);
//				AOutput.log("Migration Progress: " + files.size() + " file" + Misc.s(files.size()) + " left");
//			}
//			AOutput.log("Migration Complete");
//		}).start();
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
