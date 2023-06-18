package net.pitsim.bungee.guilds.controllers;

import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildBuff;
import net.pitsim.bungee.guilds.events.GuildReputationEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuffManager implements Listener {
	public static List<GuildBuff> buffList = new ArrayList<>();

	public static void registerBuff(GuildBuff buff) {
		buffList.add(buff);
	}

	public static GuildBuff getBuff(String refName) {
		for(GuildBuff buff : buffList) if(buff.refName.equalsIgnoreCase(refName)) return buff;
		return null;
	}

	public static GuildBuff getBuff(int index) {
		if(index >= buffList.size()) return null;
		return buffList.get(index);
	}

	public static Map<GuildBuff, Map<GuildBuff.SubBuff, Double>> getAllBuffs(int level) {
		Map<GuildBuff, Map<GuildBuff.SubBuff, Double>> allBuffMap = new LinkedHashMap<>();
		for(GuildBuff guildBuff : buffList) {
			allBuffMap.put(guildBuff, guildBuff.getBuffs(level));
		}
		return allBuffMap;
	}

	@EventHandler
	public void onReputation(GuildReputationEvent event) {
		Guild guild = event.getGuild();

		GuildBuff soulBuff = BuffManager.getBuff("soul");
		int soulBuffLevel = guild.getLevel(soulBuff);
		if(soulBuffLevel != 0) {
			Map<GuildBuff.SubBuff, Double> buffs = soulBuff.getBuffs(soulBuffLevel);
			for(Map.Entry<GuildBuff.SubBuff, Double> entry : buffs.entrySet()) {
				event.addMultiplier(1 + entry.getValue() / 100.0);
				break;
			}
		}
	}
}
