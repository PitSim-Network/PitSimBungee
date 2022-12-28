package dev.wiji.instancemanager.guilds.controllers.objects;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GuildUpgrade implements Listener {
	public String displayName;
	public String refName;
	public int maxLevel;

	public static List<Integer> slots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16));
	public int slot;

	public GuildUpgrade(String displayName, String refName, int maxLevel) {
		this.displayName = displayName;
		this.refName = refName;
		this.maxLevel = maxLevel;

		slot = slots.remove(0);

		BungeeMain.INSTANCE.getProxy().getPluginManager().registerListener(BungeeMain.INSTANCE, this);
	}

	public abstract int getCost(int level);

	public abstract DummyItemStack getBaseStack(Guild guild, int level);

	public DummyItemStack getDisplayStack(Guild guild, int level) {
		DummyItemStack itemStack = getBaseStack(guild, level);
		itemStack.setAmount(level);
		List<String> lore = itemStack.getLore();

		ALoreBuilder preLore = new ALoreBuilder();
		for(String line : preLore.getLore()) lore.add(0, line);

		ALoreBuilder postLore = new ALoreBuilder();
		postLore.addLore("");
		if(level == maxLevel) {
			if(maxLevel == 1) {
				postLore.addLore("&aUnlocked!");
			} else {
				postLore.addLore("&aMax tier unlocked!");
			}
		} else if(level == 0) {
			postLore.addLore("&7Cost: &6" + ArcticGuilds.decimalFormat.format(getCost(level + 1)) + "g");
			postLore.addLore("&7Bank Funds: &6" + ArcticGuilds.decimalFormat.format(guild.getBalance()) + "g");
			postLore.addLore("", "&eClick to purchase!");
		} else {
			postLore.addLore("&7Cost: &6" + ArcticGuilds.decimalFormat.format(getCost(level + 1)) + "g");
			postLore.addLore("&7Bank Funds: &6" + ArcticGuilds.decimalFormat.format(guild.getBalance()) + "g");
			postLore.addLore("", "&eClick to upgrade!");
		}
		lore.addAll(postLore.getLore());

		itemStack.setLore(lore);
		return itemStack;
	}
}
