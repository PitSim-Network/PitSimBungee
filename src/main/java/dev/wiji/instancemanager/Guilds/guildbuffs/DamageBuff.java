package dev.wiji.instancemanager.Guilds.guildbuffs;

import dev.wiji.instancemanager.Guilds.controllers.BuffManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildBuff;
import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.AUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;

public class DamageBuff extends GuildBuff {
	public DamageBuff() {
		super(ChatColor.RED + "Damage Buff", "damage",
				new ALoreBuilder("&7Increased damage vs other guild members").getLore(), ChatColor.RED);
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

		DummyItemStack itemStack = new DummyItemStack("DIAMOND_SWORD");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		itemStack.addModifier("HIDE_ATTRIBUTES");
		return itemStack;
	}

	@Override
	public void addBuffs() {
		SubBuff damageSub = new SubBuff("damage", "&c+%amount%% &7damage vs other guilds");
//		SubBuff trueDamageSub = new SubBuff("truedamage", "&4+%amount%\u2764 true damage");

		addSubBuff(1, damageSub, 1);
		addSubBuff(2, damageSub, 1);
		addSubBuff(3, damageSub, 1);
		addSubBuff(4, damageSub, 1);
		addSubBuff(5, damageSub, 1);
		addSubBuff(6, damageSub, 1);
		addSubBuff(7, damageSub, 1);
		addSubBuff(8, damageSub, 1);
	}
}
