package dev.wiji.instancemanager.Guilds.guildupgrades;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildUpgrade;
import dev.wiji.instancemanager.Misc.ALoreBuilder;

public class GuildBuffs extends GuildUpgrade {
	public GuildBuffs() {
		super("&bGuild Buffs", "buffs", 1);
	}

	@Override
	public int getCost(int level) {
		return 1_000_000;
	}

	@Override
	public DummyItemStack getBaseStack(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		lore.addLore("&7Unlock &bguild upgrades &7(found in /guild menu)");

		DummyItemStack itemStack = new DummyItemStack("CHEST");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}
}
