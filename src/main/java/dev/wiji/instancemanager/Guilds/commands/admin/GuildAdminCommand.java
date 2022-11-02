package dev.wiji.instancemanager.Guilds.commands.admin;

import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildAdminCommand extends AMultiCommand {
	public GuildAdminCommand(String executor) {
		super(executor);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		execute(sender, Arrays.asList(args));
	}


	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!player.hasPermission("pitsim.admin"))  {
			AOutput.color(player, "&cYou do not have permission to use this command");
			return;
		}

		super.execute(sender, new ArrayList<>(args));
	}

}
