package dev.wiji.instancemanager.Guilds.guildbuffs;

import dev.wiji.instancemanager.Guilds.controllers.BuffManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildBuff;
import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.AUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;

public class DefenceBuff extends GuildBuff {
	public DefenceBuff() {
		super(ChatColor.BLUE + "Defence Buff", "defence",
				new ALoreBuilder("&7Decreased damage from other guild members").getLore(), ChatColor.BLUE);
		firstLevelCost = 2;
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

		DummyItemStack itemStack = new DummyItemStack("DIAMOND_CHESTPLATE");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}

	@Override
	public void addBuffs() {
		SubBuff defenceSub = new SubBuff("defence", "&9-%amount%% &7damage from other guilds");
//		SubBuff trueDefenceSub = new SubBuff("truedefence", "&b-%amount%\u2764 true damage");

		addSubBuff(1, defenceSub, 1);
		addSubBuff(2, defenceSub, 1);
		addSubBuff(3, defenceSub, 1);
		addSubBuff(4, defenceSub, 1);
		addSubBuff(5, defenceSub, 1);
		addSubBuff(6, defenceSub, 1);
		addSubBuff(7, defenceSub, 1);
		addSubBuff(8, defenceSub, 1);
	}
}
