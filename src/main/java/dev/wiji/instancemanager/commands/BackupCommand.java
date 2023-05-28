package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.pitsim.FirestoreManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;

public class BackupCommand extends Command {
	public static final long BACKUP_COOLDOWN = 1000 * 60 * 2;

	public Map<CommandSender, Long> confirmationMap = new HashMap<>();
	public long lastManualBackup = 0;

	public BackupCommand() {
		super("backup");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("pitsim.admin")) return;
		FirestoreManager.takeItemBackup();
//		if(lastManualBackup + BACKUP_COOLDOWN > System.currentTimeMillis() || RestartManager.lastBackup + BACKUP_COOLDOWN > System.currentTimeMillis()) {
//			AOutput.color(sender, "&c&lERROR!&7 A backup is currently running! Please try again in a minute!");
//			return;
//		}
//
//
//		if(args.length < 1 || !args[0].equalsIgnoreCase("confirm")) confirmationMap.put(sender, System.currentTimeMillis());
//		if(confirmationMap.getOrDefault(sender, 0L) + 20_000 < System.currentTimeMillis()) {
//			AOutput.color(sender, "&c&lCONFIRM!&7 Run '/backup confirm' to start the backup");
//			return;
//		}
//
//		if(args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
//			try {
//				FirestoreManager.takeBackup(true);
//				FirestoreManager.takeItemBackup();
//				lastManualBackup = System.currentTimeMillis();
//				AOutput.color(sender, "&6&lBACKUP!&7 Started taking a backup");
//			} catch(IOException e) {
//				throw new RuntimeException(e);
//			}
//		} else {
//			AOutput.color(sender, "&c&lCONFIRM!&7 Run '/backup confirm' to start the backup");
//		}
	}
}
