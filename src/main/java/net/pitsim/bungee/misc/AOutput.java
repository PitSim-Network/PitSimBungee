package net.pitsim.bungee.misc;

import net.pitsim.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AOutput {

	public static void color(CommandSender sender, String message) {
		if(sender == null) return;
		if(!(sender instanceof ProxiedPlayer)) {
			System.out.println(message);
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
		player.sendMessage(components);
	}

	public static void error(CommandSender sender, String message) {
		color(sender, "&c" + message);
	}

	public static void log(String message) {
		BungeeMain.INSTANCE.getProxy().getConsole().sendMessage(
				"[" + BungeeMain.INSTANCE.getDescription().getName() + "] " + ChatColor.translateAlternateColorCodes('&', message));
	}
}
