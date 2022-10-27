package dev.wiji.instancemanager.Guilds.controllers.objects;

import dev.wiji.instancemanager.Guilds.enums.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class DummyItemStack {

	private String material;
	private int amount;
	private String displayName;
	private List<String> lore;
	private short data;
	private final List<String> modifiers = new ArrayList<>();

	public DummyItemStack(String material) {
		this.material = material;
		this.amount = 1;
		this.data = -1;
	}

	public DummyItemStack(String material, int amount, short data) {
		this.material = material;
		this.amount = amount;
		this.data = data;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public void setData(short data) {
		this.data = data;
	}

	public String getMaterial() {
		return material;
	}

	public int getAmount() {
		return amount;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<String> getLore() {
		return lore;
	}

	public short getData() {
		return data;
	}

	public List<String> getModifiers() {
		return modifiers;
	}

	public void addModifier(String modifier) {
		modifiers.add(modifier);
	}

	public String toString() {
		return material + "|" + amount + "|" + data + "|" + displayName + "|" + modifiers + "|" + lore;
	}

	public DyeColor getBannerColor() {
		for(String modifier : modifiers) {
			if(!modifier.startsWith("BANNER_COLOR")) continue;

			String[] split = modifier.split(":");
			return DyeColor.getByDyeData(Byte.parseByte(split[1]));
		}
		return null;
	}



}
