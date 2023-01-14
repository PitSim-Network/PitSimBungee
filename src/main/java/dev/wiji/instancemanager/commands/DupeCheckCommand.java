package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.storage.dupe.DupeManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DupeCheckCommand extends Command {
	public Map<CommandSender, Long> confirmationMap = new HashMap<>();

	public DupeCheckCommand() {
		super("dupecheck");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("pitsim.admin")) return;

		if(DupeManager.isRunning) {
			AOutput.color(sender, "&c&lERROR!&7 The dupe detection system is already running");
			return;
		}

		if(args.length >= 1 && !args[0].equalsIgnoreCase("confirm")) confirmationMap.put(sender, System.currentTimeMillis());
		if(confirmationMap.getOrDefault(sender, 0L) + 20_000 < System.currentTimeMillis()) {
			AOutput.color(sender, "&c&lCONFIRM!&7 Run '/dupecheck confirm' to start checking");
			return;
		}

		if(args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
			DupeManager.run();
			AOutput.color(sender, "&c&lERROR!&7 Started checking for duped items");
			((ProxyRunnable) () -> AOutput.color(sender,
					"&9&lNOTE!&7 It is possible for this to false flag when players are online")).runAfter(1, TimeUnit.SECONDS);
		} else {
			AOutput.color(sender, "&c&lCONFIRM!&7 Run '/dupecheck confirm' to start checking");
		}
	}
}
