package net.pitsim.bungee.misc;

import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AInventoryBuilder {
	private final Map<Integer, DummyItemStack> inventory;

	/**
	 * Builds an inventory from the ground.
	 */
	public AInventoryBuilder(ProxiedPlayer owner, int size, String name) {

		inventory = new HashMap<>();
	}

	/**
	 * Build around a pre-existing inventory.
	 */
	public AInventoryBuilder(Map<Integer, DummyItemStack> inventory) {

		this.inventory = inventory;
	}

	/**
	 * Creates a border going around the outside of the GUI.
	 */
	public AInventoryBuilder createBorder(String material, int toData, int inventorySize) {

		byte data = (byte) toData;

		for(int i = 0; i < inventorySize; i++) {

			if(i < 9 || i > inventorySize - 10 || i % 9 == 8 || i % 9 == 0) {

				inventory.put(i, new DummyItemStack(material, 1, data));
			}
		}

		return this;
	}

	/**
	 * Sets any amount of slots in the inventory.
	 */
	public AInventoryBuilder setSlots(String material, int toData, int... slots) {

		for(int slot : slots) {

			setSlot(material, toData, slot, "", null);
		}

		return this;
	}

	/**
	 * Sets a single slot in the inventory.
	 *
	 * @param name the name of the item; accepts null for no name
	 * @param lore the lore of the item; accepts null for no lore
	 */
	public AInventoryBuilder setSlot(String material, int toData, int slot, String name, List<String> lore) {

		byte data = (byte) toData;

		DummyItemStack item = new DummyItemStack(material, 1, data);
		item.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setLore(lore);

		inventory.put(slot, item);

		return this;
	}

	/**
	 * Adds an enchant glint to the items in given inventory slots.
	 *
	 * @param hideFlag whether to add the item flag that hides enchants
	 */
	public AInventoryBuilder addEnchantGlint(boolean hideFlag, int... slots) {

		for(int slot : slots) {

			DummyItemStack item = inventory.get(slot);
			AItemStackBuilder itemStackBuilder = new AItemStackBuilder(item).addEnchantGlint(hideFlag);
			inventory.put(slot, itemStackBuilder.getItemStack());
		}

		return this;
	}

	public Map<Integer, DummyItemStack> getInventory() {

		return inventory;
	}
}
