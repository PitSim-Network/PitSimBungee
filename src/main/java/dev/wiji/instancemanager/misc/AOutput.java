package dev.wiji.instancemanager.misc;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AOutput {

	public static void color(ProxiedPlayer player, String message) {
		BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
		player.sendMessage(components);
	}

	public static void error(ProxiedPlayer player, String message) {
		BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&c" + message));
		player.sendMessage(components);
	}

}
