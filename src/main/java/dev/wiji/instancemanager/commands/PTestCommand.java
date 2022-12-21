package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.pitsim.IdentificationManager;
import dev.wiji.instancemanager.storage.StorageManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
	}
}
