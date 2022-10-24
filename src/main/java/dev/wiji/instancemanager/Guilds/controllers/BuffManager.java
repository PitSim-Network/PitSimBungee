package dev.wiji.instancemanager.Guilds.controllers;

import dev.wiji.instancemanager.Guilds.controllers.objects.GuildBuff;
import net.md_5.bungee.api.plugin.Listener;

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
}
