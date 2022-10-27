package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public abstract class PreparedInventoryPanel {

	public ProxiedPlayer player;
	public PreparedGUI gui;
	public PreparedInventoryPanel previousGUI;
	public AInventoryBuilder inventoryBuilder;

	private Map<Integer, DummyItemStack> inventory;

	public boolean cancelClicks = true;

	public PreparedInventoryPanel(PreparedGUI gui) {
		this(gui, false);
	}

	/**
	 * @param lateBuild used to prevent the constructor from creating the inventory.
	 *                  The method must be called manually in the subclass
	 *                  construction with the method
	 */

	public PreparedInventoryPanel(PreparedGUI gui, boolean lateBuild) {
		this.player = gui.player;
		this.gui = gui;

		if(!lateBuild) buildInventory();
	}

	public abstract String getName();
	public abstract int getRows();

	public abstract void onClick(InventoryClickEvent event);

	public abstract void onOpen(InventoryOpenEvent event);

	public abstract void onClose(InventoryCloseEvent event);

	public void openPanel(PreparedInventoryPanel guiPanel) {

		guiPanel.previousGUI = this;

		//TODO: Call the panel's "sendPanelToPlayer" method instead of this
//		guiPanel.player.openInventory(guiPanel.getInventory());
	}

	public void openPreviousGUI() {
		if(previousGUI == null) return;

//		previousGUI.player.openInventory(previousGUI.getInventory());
		//TODO: Send message to open previous gui ("sendPanelToPlayer" method)
		previousGUI = null;
	}

	public void updateInventory() {

		//TODO: Send update message to player
//		player.updateInventory();
	}

	private static int getSlots(int rows) {

		return Math.max(Math.min(rows, 6), 1) * 9;
	}

	public Map<Integer, DummyItemStack> getInventory() {
		return inventory;
	}

	public void buildInventory() {

		//TODO: Make this method build the inventory map into a plugin message
//		inventory = Bukkit.createInventory(this, getSlots(getRows()), getName());
		//inventoryBuilder = new AInventoryBuilder(inventory);
	}

	public void sendPanelToPlayer() {
		//TODO: Send items and inventory panel info in plugin message here
	}

	public void closeInventory() {
		//TODO: Send message to player to close inventory
	}

	public void playSound(String soundString) {
		//TODO: Send message to player to play sound
	}

}
