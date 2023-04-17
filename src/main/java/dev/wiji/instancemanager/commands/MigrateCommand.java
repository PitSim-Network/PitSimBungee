package dev.wiji.instancemanager.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class MigrateCommand extends Command {

	public MigrateCommand() {
		super("migrate");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
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
