package dev.wiji.instancemanager.Guilds.controllers;

import dev.wiji.instancemanager.Guilds.controllers.objects.GuildUpgrade;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.List;

public class UpgradeManager implements Listener {
	public static List<GuildUpgrade> upgradeList = new ArrayList<>();

	public static void registerUpgrade(GuildUpgrade upgrade) {
		upgradeList.add(upgrade);
	}

	public static GuildUpgrade getUpgrade(String refName) {
		for(GuildUpgrade upgrade : upgradeList) if(upgrade.refName.equalsIgnoreCase(refName)) return upgrade;
		return null;
	}
}
