package net.pitsim.bungee.misc;

import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSerializer {
	public static LimitedItemStack deserialize(String p) {
		String[] a = p.split("\t");
		return new LimitedItemStack(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
	}

	public static String serialize(LimitedItemStack itemStack) {
		String[] parts = new String[7];
		parts[0] = itemStack.material.name();
		parts[1] = Integer.toString(itemStack.amount);
		parts[2] = String.valueOf(itemStack.durability);
		parts[3] = itemStack.displayName;
		parts[4] = itemStack.materialData;
		parts[5] = getEnchants(itemStack);
		parts[6] = itemStack.nbtData.toString();
		return StringUtils.join(parts, "\t");
	}

	public static Map<Enchantment, Integer> getEnchants(String enchantString) {
		Map<Enchantment, Integer> enchantMap = new HashMap<>();
		for(String enchantAndLevel : enchantString.split(",")) {
			String[] enchantAndLevelArr = enchantAndLevel.split(":");
			Enchantment enchant = Enchantment.getByName(enchantAndLevelArr[0]);
			int level;
			try {
				level = Integer.parseInt(enchantAndLevelArr[1]);
			} catch(Exception ignored) {
				continue;
			}
			enchantMap.put(enchant, level);
		}
		return enchantMap;
	}

	public static String getEnchants(LimitedItemStack itemStack) {
		List<String> enchants = new ArrayList<>();
		Map<Enchantment, Integer> enchantMap = itemStack.enchantMap;
		for(Enchantment enchant : enchantMap.keySet()) enchants.add(enchant.getName() + ":" + enchantMap.get(enchant));
		return StringUtils.join(enchants, ",");
	}

	public static class LimitedItemStack {
		public Material material;
		public int amount;
		public short durability;
		public String displayName;
		public String materialData;
		public Map<Enchantment, Integer> enchantMap;
		public NBTTagCompound nbtData;

		public LimitedItemStack(String material, String amount, String durability, String displayName, String materialData, String enchantString, String nbtData) {
			this.material = Material.getMaterial(material);
			this.amount = Integer.parseInt(amount);
			this.durability = Short.parseShort(durability);
			this.displayName = displayName;
			this.materialData = materialData;
			this.enchantMap = getEnchants(enchantString);
			try {
				this.nbtData = MojangsonParser.parse(nbtData);
			} catch(MojangsonParseException ignored) {}
		}
	}
}
