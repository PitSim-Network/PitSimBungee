package net.pitsim.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class LobbiesCommand extends Command {
	public static boolean overridePlayers = false;

	public LobbiesCommand(Plugin bungeeMain) {
		super("lobbies");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) commandSender;
		if(!player.hasPermission("pitsim.admin")) return;

		BaseComponent[] message;
		if(overridePlayers) {
			overridePlayers = false;
			message = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes(
					'&', "&2&lLOBBIES!&7 Disabled lobby force open!"));
		} else {
			overridePlayers = true;
			message = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes(
					'&', "&2&lLOBBIES!&7 Enabled lobby force open!"));
		}
		player.sendMessage(message);
	}
}
