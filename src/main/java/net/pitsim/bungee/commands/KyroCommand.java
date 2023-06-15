package net.pitsim.bungee.commands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.discord.DiscordManager;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.Misc;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class KyroCommand extends Command {
	public KyroCommand(Plugin bungeeMain) {
		super("kyro");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!Misc.isKyro(player.getUniqueId())) return;

		if(args.length == 0) {
			AOutput.error(player, "&c&lERROR!&7 Usage: <discord|togglecommands|graph>");
			return;
		}

		String command = args[0].toLowerCase();
		if(command.equals("discord")) {
			if(DiscordManager.isEnabled) {
				AOutput.color(player, "&c&lERROR!&7 The discord bot is already enabled");
				return;
			}
			BungeeMain.INSTANCE.getProxy().getPluginManager().registerListener(BungeeMain.INSTANCE, new DiscordManager());
			AOutput.color(player, "&9&lDISCORD!&7 Enabling discord bot");
		} else if(command.equals("togglecommands")) {
			if(DiscordManager.commandsEnabled) {
				AOutput.color(player, "&9&lDISCORD!&7 Disabling discord commands");
			} else {
				AOutput.color(player, "&9&lDISCORD!&7 Enabling discord commands");
			}
			DiscordManager.commandsEnabled = !DiscordManager.commandsEnabled;
		}
	}
}
