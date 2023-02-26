package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Misc {
	public static String formatDurationFull(long millis, boolean displaySeconds) {
		return formatDurationFull(Duration.ofMillis(millis), displaySeconds);
	}

	public static String formatDurationFull(Duration duration, boolean displaySeconds) {
		long millis = duration.toMillis();
		long days = millis / (24 * 60 * 60 * 1000);
		millis %= (24 * 60 * 60 * 1000);
		long hours = millis / (60 * 60 * 1000);
		millis %= (60 * 60 * 1000);
		long minutes = millis / (60 * 1000);
		millis %= (60 * 1000);
		long seconds = millis / 1000;
		if(!displaySeconds && seconds != 0) minutes++;
		if(!displaySeconds && minutes == 60) {
			minutes = 0;
			hours++;
		}
		if(!displaySeconds && hours == 24) {
			hours = 0;
			days++;
		}

		String durationString = "";
		if(days != 0) durationString += days + "d ";
		if(hours != 0) durationString += hours + "h ";
		if(minutes != 0) durationString += minutes + "m ";
		if(displaySeconds && seconds != 0) durationString += seconds + "s";
		return durationString.trim();
	}

	public static String formatDurationMostSignificant(double seconds) {
		DecimalFormat decimalFormat = new DecimalFormat("0.#");
		if(seconds < 60) return decimalFormat.format(seconds) + " seconds";
		if(seconds < 60 * 60) return decimalFormat.format(seconds / 60.0) + " minutes";
		if(seconds < 60 * 60 * 24) return decimalFormat.format(seconds / 60.0 / 60.0) + " hours";
		return decimalFormat.format(seconds / 60.0 / 60.0 / 24.0) + " days";
	}

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
