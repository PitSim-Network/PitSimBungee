package dev.wiji.instancemanager.guilds.commands;

import dev.wiji.instancemanager.CommandBlocker;
import dev.wiji.instancemanager.guilds.controllers.ChatManager;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.misc.ACommand;
import dev.wiji.instancemanager.misc.AMultiCommand;
import dev.wiji.instancemanager.misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class GuildChatCommand extends Command {
	public GuildChatCommand(String executor) {
		super(executor);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) {
			AOutput.error(player, "You are not in a guild");
			return;
		}

		if(args.length == 0) {
			AOutput.error(player, "&cYou must specify a message");
			return;
		}

		StringBuilder messageBuilder = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			messageBuilder.append(args[i]);
			if(i != args.length - 1) messageBuilder.append(" ");
		}

		guild.chat(player, messageBuilder.toString());
	}
}
