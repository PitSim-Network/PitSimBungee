package dev.wiji.instancemanager.guilds.guildbuffs;

import dev.wiji.instancemanager.guilds.controllers.BuffManager;
import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildBuff;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import dev.wiji.instancemanager.misc.AUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;

public class XPBuff extends GuildBuff {
	public XPBuff() {
		super(ChatColor.AQUA + "XP Buff", "xp",
				new ALoreBuilder("&7Increased xp from kills").getLore(), ChatColor.AQUA);
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

		DummyItemStack itemStack = new DummyItemStack("INK_SACK", 1, (byte) 12);
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}

	@Override
	public void addBuffs() {
		SubBuff xpSub = new SubBuff("xp", "&b+%amount%% XP &7from kills");
		SubBuff maxXPSub = new SubBuff("maxxp", "&b+%amount%% max XP &7from kills");

		addSubBuff(1, xpSub, 5);
		addSubBuff(2, maxXPSub, 5);
		addSubBuff(3, xpSub, 5);
		addSubBuff(4, maxXPSub, 5);
		addSubBuff(5, xpSub, 5);
		addSubBuff(6, maxXPSub, 5);
		addSubBuff(7, xpSub, 5);
		addSubBuff(8, maxXPSub, 5);
	}
}
