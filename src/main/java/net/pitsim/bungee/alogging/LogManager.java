package net.pitsim.bungee.alogging;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.discord.Constants;
import net.pitsim.bungee.discord.DiscordManager;
import net.pitsim.bungee.enums.DiscordLogChannel;
import net.pitsim.bungee.events.MessageEvent;
import net.pitsim.bungee.guilds.events.GuildChatEvent;
import net.pitsim.bungee.guilds.events.GuildCreateEvent;
import net.pitsim.bungee.misc.Misc;
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
import java.util.UUID;

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
		} else if(strings.get(0).equals("LOG_TO_DISCORD")) {
			if(!DiscordManager.isEnabled) return;
			DiscordLogChannel logChannelInfo = DiscordLogChannel.valueOf(strings.get(1));
			String message = strings.get(2);
			if(logChannelInfo == DiscordLogChannel.BAN_LOG_CHANNEL) {
				String[] split = message.split(",");
				UUID uuid = UUID.fromString(split[1]);
				String name = BungeeMain.getName(uuid, false);
				if(name == null) name = "Unknown";

				message = split[0].replaceAll("\\|", name);
			}

			TextChannel logChannel = DiscordManager.JDA.getTextChannelById(logChannelInfo.getChannelID());
			assert logChannel != null;
			logChannel.sendMessage(message).queue();
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
