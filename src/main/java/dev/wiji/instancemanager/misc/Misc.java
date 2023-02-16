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

	public static String formatDuration(long millis, boolean displaySeconds) {
		long days = millis / (24 * 60 * 60 * 1000);
		millis %= (24 * 60 * 60 * 1000);
		long hours = millis / (60 * 60 * 1000);
		millis %= (60 * 60 * 1000);
		long minutes = millis / (60 * 1000);
		millis %= (60 * 1000);
		long seconds = millis / 1000;

		String duration = "";
		if(days != 0) duration += days + "d ";
		if(hours != 0) duration += hours + "h ";
		if(minutes != 0) duration += minutes + "m ";
		if(displaySeconds && seconds != 0) duration += seconds + "s";
		return duration;
	}
}
