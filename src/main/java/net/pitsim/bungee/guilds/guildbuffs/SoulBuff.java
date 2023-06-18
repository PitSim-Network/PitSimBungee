package net.pitsim.bungee.guilds.guildbuffs;

import net.pitsim.bungee.guilds.controllers.BuffManager;
import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildBuff;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;

public class SoulBuff extends GuildBuff {
	public SoulBuff() {
		super(ChatColor.YELLOW + "Soul Buff", "soul",
				new ALoreBuilder("&7Increased soul chance").getLore(), ChatColor.YELLOW);
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

		DummyItemStack itemStack = new DummyItemStack("GHAST_TEAR");
		itemStack.setDisplayName(displayName);
		itemStack.setLore(lore.getLore());
		return itemStack;
	}

	@Override
	public void addBuffs() {
		SubBuff soulSub = new SubBuff("soul", "&e+%amount%% &7more passive soul");

		addSubBuff(1, soulSub, 12.5);
		addSubBuff(2, soulSub, 12.5);
		addSubBuff(3, soulSub, 12.5);
		addSubBuff(4, soulSub, 12.5);
		addSubBuff(5, soulSub, 12.5);
		addSubBuff(6, soulSub, 12.5);
		addSubBuff(7, soulSub, 12.5);
		addSubBuff(8, soulSub, 12.5);
	}
}
