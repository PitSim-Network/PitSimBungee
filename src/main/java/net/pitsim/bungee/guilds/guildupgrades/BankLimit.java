package net.pitsim.bungee.guilds.guildupgrades;

import net.pitsim.bungee.guilds.ArcticGuilds;
import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildUpgrade;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AUtil;
import net.md_5.bungee.api.ChatColor;

public class BankLimit extends GuildUpgrade {
	public BankLimit() {
		super(ChatColor.GOLD + "Larger Bank", "bank", 4);
	}

	@Override
	public int getCost(int level) {
		switch(level) {
			case 1:
				return 100_000;
			case 2:
				return 1_000_000;
			case 3:
				return 10_000_000;
			case 4:
				return 100_000_000;
		}
		return -1;
	}

	@Override
	public DummyItemStack getBaseStack(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		if(level != 0) {
			lore.addLore("&7Current: &6" + ArcticGuilds.decimalFormat.format(guild.getMaxBank()) + "g &7bank size", "&7Tier: &a" + AUtil.toRoman(level), "");
		}
		lore.addLore("&7Each tier:", "&6x10 &7bank size");

		DummyItemStack itemStack = new DummyItemStack("CHEST");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}
}
