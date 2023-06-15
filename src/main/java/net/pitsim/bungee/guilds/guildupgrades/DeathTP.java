package net.pitsim.bungee.guilds.guildupgrades;

import net.pitsim.bungee.guilds.ArcticGuilds;
import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildUpgrade;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AUtil;

public class DeathTP extends GuildUpgrade {
	public DeathTP() {
		super("&9Death TP", "tp", 1);
	}

	@Override
	public int getCost(int level) {
		return 10_000_000;
	}

	@Override
	public DummyItemStack getBaseStack(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		if(level != 0) {
			lore.addLore("&7Current: &6" + ArcticGuilds.decimalFormat.format(guild.getMaxBank()) + "g bank size", "&7Tier: &a" + AUtil.toRoman(level), "");
		}
		lore.addLore("&7Each tier:", "&6x10 &7bank size");

		DummyItemStack itemStack = new DummyItemStack("CHEST");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}
}
