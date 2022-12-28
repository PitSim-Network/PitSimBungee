package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

@SuppressWarnings("unused")
public class AItemStackBuilder {
	private final DummyItemStack itemStack;

	/**
	 * Builds an item from the ground.
	 */
	public AItemStackBuilder(String material) {

		this(material, 1);
	}

	/**
	 * Builds an item from the ground.
	 */
	public AItemStackBuilder(String material, int amount) {

		this(material, amount, 0);
	}

	/**
	 * Builds an item from the ground.
	 */
	public AItemStackBuilder(String material, int amount, int data) {

		itemStack = new DummyItemStack(material, amount, (short) data);
	}

	/**
	 * Build around a pre-existing item.
	 */
	public AItemStackBuilder(DummyItemStack itemStack) {

		this.itemStack = itemStack;
	}

	public AItemStackBuilder setName(String name) {

		itemStack.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

		return this;
	}

	public AItemStackBuilder setLore(List<String> lore) {

		itemStack.setLore(lore);

		return this;
	}

	public AItemStackBuilder setLore(ALoreBuilder loreBuilder) {

		itemStack.setLore(loreBuilder.getLore());

		return this;
	}

	/**
	 * Adds an enchant glint to the item.
	 *
	 * @param hideFlag whether to add the item flag that hides enchants
	 * @deprecated method literally doesn't work :/
	 */
	@Deprecated
	public AItemStackBuilder addEnchantGlint(boolean hideFlag) {

		if(itemStack.getMaterial().equals("AIR")) return this;

		itemStack.addModifier("ENCHANT_GLINT");
		if(hideFlag) {
			itemStack.addModifier("HIDE_ENCHANTS");
		}

		return this;
	}

	/**
	 * Adds the unbreakable item flag to the item.
	 *
	 * @param hideFlag whether to add the item flag that hides the unbreakable
	 */
	public AItemStackBuilder addUnbreakable(boolean hideFlag) {

		if(itemStack.getMaterial().equals("AIR")) return this;

		itemStack.addModifier("UNBREAKABLE");
		if(hideFlag) {
			itemStack.addModifier("HIDE_UNBREAKABLE");
		}

		return this;
	}

	public DummyItemStack getItemStack() {
		return itemStack;
	}

}
