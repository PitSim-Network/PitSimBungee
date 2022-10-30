package dev.wiji.instancemanager.Guilds.controllers.objects;

import dev.wiji.instancemanager.Guilds.enums.DyeColor;

import java.util.ArrayList;
import java.util.Collections;
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
		StringBuilder modifiers = new StringBuilder();

		for(int i = 0; i < this.modifiers.size(); i++) {
			modifiers.append(this.modifiers.get(i));
			if(i != this.modifiers.size() - 1) {
				modifiers.append("<");
			}
		}

		StringBuilder lore = new StringBuilder();

		if(this.lore != null) {
			for(int i = 0; i < this.lore.size(); i++) {
				lore.append(this.lore.get(i));
				if(i != this.lore.size() - 1) {
					lore.append("<");
				}
			}
		}
		
		
		return material + "|" + amount + "|" + data + "|" + displayName + "|" + modifiers + "|" + lore;
	}

	public static DummyItemStack fromString(String string) {
		String[] split = string.split("\\|");

		DummyItemStack stack = new DummyItemStack(split[0]);
		if(split.length == 1) return stack;

		stack.setAmount(Integer.parseInt(split[1]));
		stack.setData(Short.parseShort(split[2]));

		if(split.length == 3) return stack;
		stack.setDisplayName(split[3]);

		if(split.length == 4) return stack;
		String modifiers = split[4];
		for(String s : modifiers.split("<")) {
			stack.addModifier(s);
		}

		if(split.length == 5) return stack;
		List<String> loreList = new ArrayList<>();
		String lore = split[5];
		Collections.addAll(loreList, lore.split("<"));
		stack.setLore(loreList);

		return stack;
	}

	public DyeColor getBannerColor() {
		for(String modifier : modifiers) {
			if(!modifier.startsWith("BANNER")) continue;

			String[] split = modifier.split(":");
			return DyeColor.getByDyeData(Byte.parseByte(split[1]));
		}
		return null;
	}



}
