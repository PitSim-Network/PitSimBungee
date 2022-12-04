package dev.wiji.instancemanager.alogging;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Guilds.events.GuildChatEvent;
import dev.wiji.instancemanager.Guilds.events.GuildCreateEvent;
import dev.wiji.instancemanager.Misc.Misc;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ServerLogManager implements Listener {

	@EventHandler
	public void onGuildChat(GuildChatEvent event) {
		ProxiedPlayer player = event.getPlayer();
		logProxyMessage(LogType.GUILD_CHAT, "[" + event.getGuild().name + "] " + player.getName() + " >> " + event.getMessage());
	}

	@EventHandler
	public void onGuildCreate(GuildCreateEvent event) {
		ProxiedPlayer player = event.getPlayer();
		logProxyMessage(LogType.GUILD_CREATE, player.getName() + " created " + event.getGuild().name);
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		if(strings.isEmpty()) return;

		if(strings.remove(0).equals("LOG")) {
			LogType logType = LogType.valueOf(strings.remove(0));
			String serverName = strings.remove(0);
			String logMessage = strings.remove(0);
			Date date = Misc.convertToEST(new Date(event.getMessage().getLongs().remove(0)));
			logMessage(logType, serverName, logMessage, date);
		}
	}

	public static void logMessage(LogType logType, String serverName, String logMessage, Date date) {
		for(LogType.LogFile logFile : logType.logFiles) {
			try {
				String dir = BungeeMain.INSTANCE.getDataFolder() + logFile.getRelativePath(serverName, date);
				new File(dir).getParentFile().mkdirs();
				PrintWriter writer = new PrintWriter(new FileWriter(dir, true));

				writer.append(logMessage).append("\n");
				writer.close();
			} catch(IOException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	public static void logProxyMessage(LogType logType, String logMessage) {
		Date date = Misc.convertToEST(new Date());
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		logMessage(logType, null, "[" + dateFormat.format(date) + "][proxy][" + logType + "]: " + logMessage, date);
	}
}
