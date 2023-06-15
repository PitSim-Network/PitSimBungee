package net.pitsim.bungee.guilds.guildupgrades;

import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildUpgrade;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AUtil;
import net.md_5.bungee.api.ChatColor;

public class GuildSize extends GuildUpgrade {
	public GuildSize() {
		super(ChatColor.GREEN + "Increased Guild Size", "size", 6);
	}

	@Override
	public int getCost(int level) {
		switch(level) {
			case 1:
				return 1_000_000;
			case 2:
				return 10_000_000;
			case 3:
				return 25_000_000;
			case 4:
				return 50_000_000;
			case 5:
				return 100_000_000;
			case 6:
				return 1_000_000_000;
		}
		return -1;
	}

	@Override
	public DummyItemStack getBaseStack(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		if(level != 0) {
			lore.addLore("&7Current: &a+" + level + " member slots", "&7Tier: &a" + AUtil.toRoman(level), "");
		}
		lore.addLore("&7Each tier:", "&a+1 &7member slot");

		DummyItemStack itemStack = new DummyItemStack("SKULL_ITEM", 1, (byte) 3);
		itemStack.addModifier("SKULL_OWNER:" + guild.ownerUUID.toString());
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}
}
