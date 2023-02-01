package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Misc {
	public static BaseComponent[] colorize(String message) {
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static OffsetDateTime getCurrentDate() {
		return OffsetDateTime.now(BungeeMain.TIME_ZONE);
	}

	public static String getCurrentDateFormatted() {
		OffsetDateTime date = OffsetDateTime.now(BungeeMain.TIME_ZONE);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
		return dateFormat.format(date);
	}
}
