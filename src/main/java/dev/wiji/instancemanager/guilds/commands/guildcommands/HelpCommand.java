package dev.wiji.instancemanager.guilds.commands.guildcommands;

import dev.wiji.instancemanager.guilds.inventories.HelpGUI;
import dev.wiji.instancemanager.misc.ACommand;
import dev.wiji.instancemanager.misc.AMultiCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class HelpCommand extends ACommand {
	public HelpCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		new HelpGUI(player).open();
	}

}
