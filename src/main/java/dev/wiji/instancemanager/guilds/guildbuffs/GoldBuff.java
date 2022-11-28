package dev.wiji.instancemanager.guilds.guildbuffs;

import dev.wiji.instancemanager.guilds.controllers.BuffManager;
import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildBuff;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import dev.wiji.instancemanager.misc.AUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;

public class GoldBuff extends GuildBuff {
	public GoldBuff() {
		super(ChatColor.GOLD + "Gold Buff", "gold",
				new ALoreBuilder("&7Increased gold from kills").getLore(), ChatColor.GOLD);
		firstLevelCost = 3;
	}

	@Override
	public DummyItemStack getDisplayItem(Guild guild, int level) {
		ALoreBuilder lore = new ALoreBuilder();
		if(level != 0) {
			lore.addLore("&7Tier: &a" + AUtil.toRoman(level), "");
			Map<SubBuff, Double> buffMap = BuffManager.getAllBuffs(level).get(this);
			for(Map.Entry<SubBuff, Double> entry : buffMap.entrySet()) {
				lore.addLore(chatColor + entry.getKey().getDisplayString(entry.getValue()));
			}
		}

		DummyItemStack itemStack = new DummyItemStack("GOLD_INGOT");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}

	@Override
	public void addBuffs() {
		SubBuff goldSub = new SubBuff("gold", "&6+%amount%% gold &7from kills");

		addSubBuff(1, goldSub, 2);
		addSubBuff(2, goldSub, 2);
		addSubBuff(3, goldSub, 2);
		addSubBuff(4, goldSub, 4);
		addSubBuff(5, goldSub, 2);
		addSubBuff(6, goldSub, 2);
		addSubBuff(7, goldSub, 2);
		addSubBuff(8, goldSub, 4);
	}
}
