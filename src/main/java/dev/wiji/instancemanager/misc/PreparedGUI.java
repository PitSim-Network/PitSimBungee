package dev.wiji.instancemanager.misc;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PreparedGUI {
	public ProxiedPlayer player;

	private PreparedInventoryPanel homePanel;
	private final Map<String, PreparedInventoryPanel> panelMap = new HashMap<>();

	public PreparedGUI(ProxiedPlayer player) {
		this.player = player;
	}

	public void setHomePanel(PreparedInventoryPanel homePanel) {

		this.homePanel = homePanel;
	}

	public void addPanel(String refName, PreparedInventoryPanel panel) {

		panelMap.put(refName, panel);
	}

	public void open() {

		if(homePanel != null) homePanel.sendPanelToPlayer();
//		if(homePanel != null) player.openInventory(homePanel.getInventory());
	}

	public PreparedInventoryPanel getHomePanel() {

		return homePanel;
	}

	public PreparedInventoryPanel getPanel(String refName) {

		for(Map.Entry<String, PreparedInventoryPanel> entry : panelMap.entrySet()) {
			if(entry.getKey().equalsIgnoreCase(refName)) return entry.getValue();
		}
		return homePanel;
	}
}
