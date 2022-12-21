package dev.wiji.instancemanager.guilds.controllers;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.guilds.events.GuildReputationEvent;
import dev.wiji.instancemanager.misc.AData;
import dev.wiji.instancemanager.misc.APlayer;
import dev.wiji.instancemanager.misc.APlayerData;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.guilds.events.GuildReputationEvent;
import dev.wiji.instancemanager.misc.AData;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GuildManager implements Listener {
	public static List<Guild> guildList = new ArrayList<>();
	public static List<GuildMember> guildMemberList = new ArrayList<>();
	public static AData guildFile;
	private static List<Guild> topGuilds = new ArrayList<>();

	static {
		guildFile = new AData("guilds", "", false);

		for(String key : guildFile.getConfiguration().getKeys()) {
			Configuration guildData = guildFile.getConfiguration().getSection(key);
			new Guild(key, guildData);
		}

		new ProxyRunnable() {
			@Override
			public void run() {
//				TODO: Call this code on buff/upgrade change
				for(Guild guild : guildList) {
					guild.diminish();
//					for(Map.Entry<GuildMember, GuildMemberInfo> entry : guild.members.entrySet()) {
//						UUID playerUUID = entry.getKey().playerUUID;
//						TODO: Send PluginMessage to frontend updating open Guild GUIs
//						for(ProxiedPlayer player : BungeeMain.getMainGamemodePlayers()) {
//							if(!player.getUniqueId().equals(playerUUID)) continue;
//							if(player.getOpenInventory().getTopInventory().getHolder().getClass() != BuffPanel.class)
//								continue;
//							BuffPanel buffPanel = (BuffPanel) player.getOpenInventory().getTopInventory().getHolder();
//							buffPanel.setInventory();
//						}
//					}
				}
			}
		}.runAfterEvery(5, 5, TimeUnit.SECONDS);

		((ProxyRunnable) () -> {
			for(Guild guild : GuildManager.guildList) {
				if(guild.queuedReputation == 0) continue;

				GuildReputationEvent event = new GuildReputationEvent(guild, guild.queuedReputation);
				BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(event);

				guild.queuedReputation = 0;
				guild.addReputationDirect(event.getTotalReputation());
			}
		}).runAfterEvery(0, 10, TimeUnit.SECONDS);

		((ProxyRunnable) GuildManager::sortGuilds).runAfterEvery(0, 60, TimeUnit.SECONDS);
	}

	public static Guild getGuildFromGuildUUID(UUID guildUUID) {
		for(Guild guild : guildList) if(guild.uuid.equals(guildUUID)) return guild;
		return null;
	}

	public static Guild getGuildFromPlayer(UUID playerUUID) {
		GuildMember member = GuildManager.getMember(playerUUID);
		UUID guildUUID = member.getGuildUUID();
		if(guildUUID == null) return null;
		return getGuildFromGuildUUID(guildUUID);
	}

	public static GuildMember getMember(UUID playerUUID) {
		for(GuildMember guildMember : guildMemberList) {
			if(!guildMember.playerUUID.equals(playerUUID)) continue;
			return guildMember;
		}
		return new GuildMember(playerUUID, null);
	}

	private static void sortGuilds() {
		topGuilds.clear();
		input:
		for(Guild guild : guildList) {
			for(int i = 0; i < topGuilds.size(); i++) {
				Guild topGuild = topGuilds.get(i);
				if(topGuild.reputation >= guild.reputation) continue;
				topGuilds.add(i, guild);
				continue input;
			}
			topGuilds.add(guild);
		}
	}

	public static List<Guild> getTopGuilds() {
		sortGuilds();
		return topGuilds;
	}

	public static int getRank(Guild guild) {
		for(int i = 0; i < topGuilds.size(); i++) {
			Guild testGuild = topGuilds.get(i);
			if(testGuild == guild) return i;
		}
		return -1;
	}
}
