package dev.wiji.instancemanager.storage.dupe;

import net.md_5.bungee.api.ChatColor;

public abstract class ItemLocation {

	public abstract String getLocation();

	public String getUnformattedLocation() {
		return ChatColor.stripColor(getLocation());
	}

	public static class InventoryLocation extends ItemLocation {
		public int slot;

		public InventoryLocation(int slot) {
			this.slot = slot;
		}

		@Override
		public String getLocation() {
			return "&eInventory &7(Slot &e" + slot + "&7)";
		}
	}

	public static class ArmorLocation extends ItemLocation {
		public int slot;

		public ArmorLocation(int slot) {
			this.slot = slot;
		}

		@Override
		public String getLocation() {
			return "&fArmor &7(Slot &f" + slot + "&d)";
		}
	}

	public static class EnderchestLocation extends ItemLocation {
		public int enderchest;
		public int slot;

		public EnderchestLocation(int enderchest, int slot) {
			this.enderchest = enderchest;
			this.slot = slot;
		}

		@Override
		public String getLocation() {
			return ChatColor.translateAlternateColorCodes('&', "&dEnderchest " + enderchest + " &7(Slot &d" + slot + "&7)");
		}
	}
}