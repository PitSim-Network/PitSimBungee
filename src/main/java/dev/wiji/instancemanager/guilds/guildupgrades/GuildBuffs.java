package dev.wiji.instancemanager.guilds.guildupgrades;

import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildUpgrade;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import net.md_5.bungee.api.ChatColor;

public class GuildBuffs extends GuildUpgrade {
	public GuildBuffs() {
		super(ChatColor.AQUA + "Guild Buffs", "buffs", 1);
	}

	@Override
	public int getCost(int level) {
		return 1_000_000;
	}

	@Override
	public DummyItemStack getBaseStack(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		lore.addLore("&7Unlock &bguild upgrades &7(found in /guild menu)");

		DummyItemStack itemStack = new DummyItemStack("BEACON");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}
}
