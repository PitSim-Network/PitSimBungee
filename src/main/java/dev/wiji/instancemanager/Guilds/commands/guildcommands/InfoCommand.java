package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import de.sumafu.PlayerStatus.PlayerNeverConnectedException;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMessageBuilder;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InfoCommand extends ACommand {
	public InfoCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild guild = null;
		if(!args.isEmpty()) {
			String guildName = args.get(0);
			for(Guild testGuild : GuildManager.guildList) {
				if(!testGuild.name.equalsIgnoreCase(guildName)) continue;
				guild = testGuild;
				break;
			}
			if(guild == null) {
				UUID uuid = null;
				try {
					uuid = BungeeMain.psApi.getUuidOfName(guildName);
				} catch(PlayerNeverConnectedException | SQLException ignored) { }

				if(uuid == null) {
					AOutput.color(player, "Could not find that guild/player");
					return;
				}
				Guild testGuild = GuildManager.getGuildFromGuildUUID(uuid);
				if(testGuild == null) {
					AOutput.color(player, "That player is not in a guild");
					return;
				}
				guild = testGuild;
			}
		}
		if(guild == null) {
			guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
			if(guild == null) {
				AOutput.color(player, "You are not in a guild");
				return;
			}
		}

		List<Map.Entry<GuildMember, GuildMemberInfo>> sortedPlayers = new ArrayList<>();
		List<Map.Entry<GuildMember, GuildMemberInfo>> clone = new ArrayList<>();
		clone.addAll(guild.members.entrySet());
		main:
		for(Map.Entry<GuildMember, GuildMemberInfo> entry : guild.members.entrySet()) {
			if(sortedPlayers.isEmpty()) {
				sortedPlayers.add(entry);
				continue;
			}
			for(int i = 0; i < clone.size(); i++) {
				Map.Entry<GuildMember, GuildMemberInfo> toCheckMember = clone.get(i);
				if(i >= sortedPlayers.size()) {
					sortedPlayers.add(entry);
					continue main;
				} else if(entry.getValue().rank.isAtLeast(toCheckMember.getValue().rank) && entry.getValue().rank != toCheckMember.getValue().rank) {
					sortedPlayers.add(i, entry);
					continue main;
				}
			}
		}
		List<UUID> onlinePlayers = new ArrayList<>();
		for(Map.Entry<GuildMember, GuildMemberInfo> entry : sortedPlayers) {
//			onlinePlayers.add(entry.getKey().playerUUID);
//			if(true) continue;
			ProxiedPlayer onlinePlayer = BungeeMain.INSTANCE.getProxy().getPlayer(entry.getKey().playerUUID);
			if(onlinePlayer == null || BungeeVanishAPI.isInvisible(onlinePlayer)) continue;
			onlinePlayers.add(entry.getKey().playerUUID);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone(ConfigManager.configuration.getString("timezone")));
		AMessageBuilder messageBuilder = null;
		try {
			messageBuilder = new AMessageBuilder()
					.addLine("&a&lGUILD " + guild.getColor() + guild.name)
					.addLine(guild.getColor() + " * &7Date Created: " + guild.getColor() + dateFormat.format(guild.dateCreated))
					.addLine(guild.getColor() + " * &7Guild Rank: " + guild.getColor() + guild.getFormattedRank())
					.addLine(guild.getColor() + " * &7Guild Reputation: " + guild.getColor() + guild.reputation)
					.addLine(guild.getColor() + " * &7Reputation Points: " + guild.getColor() + guild.getTotalBuffCost() +
							"&7/" + guild.getColor() + guild.getRepPoints())
					.addLine(guild.getColor() + " * &7Bank Balanace: &6" + guild.getFormattedBalance() + "g&7/&6" + ArcticGuilds.decimalFormat.format(guild.getMaxBank()))
					.addLine(guild.getColor() + " * &7Owner: " + guild.getColor() + BungeeMain.psApi.getNameOfUuid(guild.ownerUUID))
					.addLine(guild.getColor() + " * &7Members: &7(" + guild.getColor() + guild.members.size() + "&7/" + guild.getColor() + guild.getMaxMembers() + "&7)")
					.addLine(guild.getColor() + " * &7Online Members: &7(" + guild.getColor() + onlinePlayers.size() + "&7/" + guild.getColor() + guild.members.size() + "&7)");
		} catch(SQLException | PlayerNeverConnectedException e) {
			throw new RuntimeException(e);
		}
		for(Map.Entry<GuildMember, GuildMemberInfo> entry : sortedPlayers) {
			if(!onlinePlayers.contains(entry.getKey().playerUUID)) continue;
			UUID guildPlayerUUID = entry.getKey().playerUUID;
			messageBuilder.addLine(guild.getColor() + "    - &a" + entry.getValue().rank.prefix + BungeeMain.getName(guildPlayerUUID, true));
		}
		messageBuilder.addLine(guild.getColor() + " * &7Offline Members: &7(" + guild.getColor() +
				(guild.members.size() - onlinePlayers.size()) + "&7/" + guild.getColor() + guild.members.size() + "&7)");
		for(Map.Entry<GuildMember, GuildMemberInfo> entry : sortedPlayers) {
			if(onlinePlayers.contains(entry.getKey().playerUUID)) continue;
			UUID guildPlayerUUID = entry.getKey().playerUUID;
			messageBuilder.addLine(guild.getColor() + "    - &c" + entry.getValue().rank.prefix + BungeeMain.getName(guildPlayerUUID, true));
		}

		messageBuilder.colorize().send(player);
	}

}
