package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Objects.PluginMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PreparedInventoryPanel {

	public static List<PreparedInventoryPanel> panels = new ArrayList<>();

	public ProxiedPlayer player;
	public PreparedGUI gui;
	public PreparedInventoryPanel previousGUI;

	public long creationTime;

	private final Map<Integer, DummyItemStack> inventory = new HashMap<>();
	public AInventoryBuilder inventoryBuilder = new AInventoryBuilder(inventory);

	public boolean cancelClicks = true;

	public PreparedInventoryPanel(PreparedGUI gui) {
		this(gui, false);
		panels.add(this);

		this.creationTime = System.currentTimeMillis();
	}

	/**
	 * @param lateBuild used to prevent the constructor from creating the inventory.
	 *                  The method must be called manually in the subclass
	 *                  construction with the method
	 */

	public PreparedInventoryPanel(PreparedGUI gui, boolean lateBuild) {
		this.player = gui.player;
		this.gui = gui;

		panels.add(this);

		if(!lateBuild) buildInventory();
	}

	public abstract String getName();
	public abstract int getRows();

	public abstract void onClick(InventoryClickEvent event);

	public abstract void onOpen(InventoryOpenEvent event);

	public abstract void onClose(InventoryCloseEvent event);

	public void openPanel(PreparedInventoryPanel guiPanel) {
		guiPanel.previousGUI = this;

		guiPanel.sendPanelToPlayer();
	}

	public void openPreviousGUI() {
		if(previousGUI == null) return;

		openPanel(previousGUI);
		previousGUI = null;
	}

	public void updateInventory(PreparedInventoryPanel panel) {
		panel.previousGUI = previousGUI;
		panel.sendPanelToPlayer();
	}

	private static int getSlots(int rows) {

		return Math.max(Math.min(rows, 6), 1) * 9;
	}

	public Map<Integer, DummyItemStack> getInventory() {
		return inventory;
	}

	public PluginMessage buildInventory() {
		PluginMessage message = new PluginMessage();
		message.writeString("OPEN INVENTORY");
		message.writeString(player.getUniqueId().toString());
		message.writeString(getName());
		message.writeInt(getRows());

		for(int i = 0; i <= getSlots(getRows()); i++) {
			DummyItemStack item = inventory.get(i);
			if(item == null) {
				message.writeString("null");
			} else {
				message.writeString(item.toString());
			}
		}

		String server = player.getServer().getInfo().getName();
		message.addServer(server);

		return message;
	}

	public void sendPanelToPlayer() {
//		for(PreparedInventoryPanel panel : panels) {
//			if(panel.player.equals(player)) {
//				panels.remove(panel);
//				break;
//			}
//		}

		buildInventory().send();
	}

	public void closeInventory() {
		PluginMessage message = new PluginMessage();
		message.writeString("CLOSE INVENTORY");
		message.writeString(player.getUniqueId().toString());
		message.addServer(player.getServer().getInfo().getName());
		message.send();
	}

	public void playSound(String soundString) {
		PluginMessage message = new PluginMessage();
		message.writeString("PLAY SOUND");
		message.writeString(player.getUniqueId().toString());
		message.writeString(soundString);
		message.addServer(player.getServer().getInfo().getName());
		message.send();
	}

}
