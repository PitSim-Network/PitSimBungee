package net.pitsim.bungee.guilds.commands.admin;

import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class ReputationCommand extends ACommand {
	public ReputationCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(args.size() < 2) {
			AOutput.color(player, "Usage: /rep <guild> <amount>");
			return;
		}

		Guild guild = null;
		for(Guild testGuild : GuildManager.guildList) {
			if(!testGuild.name.equalsIgnoreCase(args.get(0))) continue;
			guild = testGuild;
			break;
		}
		if(guild == null) {
			AOutput.color(player, "Could not find a guild with that name");
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(1));
		} catch(Exception ignored) {
			AOutput.color(player, "Invalid amount");
			return;
		}

		guild.addReputationDirect(amount);
		AOutput.color(player, "&a&lGUILD! &7Gave the guild " + guild.name + " " + amount + " reputation");
	}

}
