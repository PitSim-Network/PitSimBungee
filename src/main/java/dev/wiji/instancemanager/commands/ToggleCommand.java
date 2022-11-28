package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.skywars.SkywarsGameManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class ToggleCommand extends Command {
	public ToggleCommand(Plugin bungeeMain) {
		super("toggle");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) commandSender;
		if(!player.hasPermission("pitsim.toggle")) return;

		if(strings.length != 1) {
			commandSender.sendMessage((new ComponentBuilder("Invalid args!").color(ChatColor.RED).create()));
			return;
		}

		if(strings[0].equalsIgnoreCase("skywars")) {
			if(SkywarsGameManager.isEnabled){
				SkywarsGameManager.isEnabled = false;
				commandSender.sendMessage((new ComponentBuilder("Turned off queuing for Skywars").color(ChatColor.RED).create()));
			}
			else {
				SkywarsGameManager.isEnabled = true;
				commandSender.sendMessage((new ComponentBuilder("Turned on queuing for Skywars").color(ChatColor.GREEN).create()));
			}
		}

		if(strings[0].equalsIgnoreCase("dev")) {
			if(DevCommand.isEnabled) {
				DevCommand.isEnabled = false;
				commandSender.sendMessage((new ComponentBuilder("Turned off queuing for Dev").color(ChatColor.RED).create()));
			} else {
				DevCommand.isEnabled = true;
				commandSender.sendMessage((new ComponentBuilder("Turned on queuing for Dev").color(ChatColor.GREEN).create()));
			}
		}
	}
}
