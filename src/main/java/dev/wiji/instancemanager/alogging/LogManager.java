package dev.wiji.instancemanager.alogging;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.discord.Constants;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.guilds.events.GuildChatEvent;
import dev.wiji.instancemanager.guilds.events.GuildCreateEvent;
import dev.wiji.instancemanager.misc.Misc;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.List;

public class LogManager implements Listener {

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
	public void onCommandSend(ChatEvent event) {
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		if(!event.isCommand()) {
			if(!ConfigManager.isDev() && !event.isCancelled()) {
				String message = ChatColor.stripColor(event.getMessage()).replaceAll("`", "");
				logChatToDiscord("```" + Misc.getCurrentDateFormatted() + " [" + player.getServer().getInfo().getName() + "] " +
						player.getName() + " >> " + message + "```");
			}
			return;
		}

		String message = player.getName() + " executed ";
		if(event.isCancelled()) message += "(cancelled) ";
		logProxyMessage(LogType.PLAYER_COMMAND, message + event.getMessage().toLowerCase());
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		if(strings.isEmpty()) return;

		if(strings.get(0).equals("LOG")) {
			LogType logType = LogType.valueOf(strings.get(1));
			String serverName = strings.get(2);
			String logMessage = strings.get(3);
			logMessage(logType, serverName, logMessage, Misc.getCurrentDate());
		}
	}

	public static void logMessage(LogType logType, String serverName, String logMessage, OffsetDateTime date) {
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
		logMessage(logType, null, "[" + Misc.getCurrentDateFormatted() + "][proxy][" + logType + "]: " + logMessage, Misc.getCurrentDate());
	}

	public static void logChatToDiscord(String logMessage) {
		TextChannel textChannel = DiscordManager.MAIN_GUILD.getTextChannelById(Constants.INGAME_CHAT_CHANNEL);
		assert textChannel != null;
		textChannel.sendMessage(logMessage).queue();
	}
}
