package dev.wiji.instancemanager.misc;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Misc {
	public static BaseComponent[] colorize(String message) {
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static Date convertToEST(Date date) {
		DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
		formatter.setTimeZone(TimeZone.getTimeZone("EST"));
		try {
			return formatter.parse((formatter.format(date)));
		} catch(ParseException exception) {
			return null;
		}
	}
}
