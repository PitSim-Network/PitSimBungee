package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Misc {
	public static boolean isKyro(UUID uuid) {
		List<UUID> kyroAccounts = new ArrayList<>();
//		KyroKrypt
		kyroAccounts.add(UUID.fromString("01acbb49-6357-4502-81ca-e79f4b31a44e"));
//		BHunter
		kyroAccounts.add(UUID.fromString("a7d2e208-e475-40bf-94d7-f96c6e1238a8"));
//		UUIDSpoof
		kyroAccounts.add(UUID.fromString("1088c509-e8e0-4b7b-8faa-b9f3f72b06f6"));
//		PayForTruce
		kyroAccounts.add(UUID.fromString("777566ed-d4ad-4cf1-a4d9-0e37769357df"));
//		Fishduper
		kyroAccounts.add(UUID.fromString("1db946e6-edfe-42ac-9fd6-bf135aa5130e"));
		return kyroAccounts.contains(uuid);
	}

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
		if(!displaySeconds) {
			if(seconds != 0) minutes++;
			if(minutes == 60) {
				minutes = 0;
				hours++;
			}
			if(hours == 24) {
				hours = 0;
				days++;
			}
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

	public static String longToTimeFormatted (long time) {
		long days = time / (24 * 60 * 60 * 1000);
		time = time % (24 * 60 * 60 * 1000);
		long hours = time / (60 * 60 * 1000);
		time = time % (60 * 60 * 1000);
		long minutes = time / (60 * 1000);
		time = time % (60 * 1000);
		long seconds = time / 1000;
		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
	}

	public static String s(double value) {
		return value == 1 ? "" : "s";
	}

	public static Map<UUID, String> rankColorMap = new HashMap<>();
	public static String getRankColor(UUID uuid) {
		if(rankColorMap.containsKey(uuid)) return rankColorMap.get(uuid);
		try {
			String rankColor = BungeeMain.LUCKPERMS.getUserManager().loadUser(uuid).get().getCachedData().getMetaData().getPrefix();
//			TODO: Check-in with players to make sure this fixed it and it wasn't returning "null" as a string
			if(rankColor == null) throw new Exception();
			rankColorMap.put(uuid, rankColor);
			return rankColor;
		} catch(Exception ignored) {
			return "&7";
		}
	}
}
