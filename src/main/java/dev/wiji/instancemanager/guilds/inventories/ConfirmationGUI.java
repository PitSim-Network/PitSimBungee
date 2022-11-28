package dev.wiji.instancemanager.guilds.inventories;

import dev.wiji.instancemanager.misc.ALoreBuilder;
import dev.wiji.instancemanager.misc.PreparedGUI;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ConfirmationGUI extends PreparedGUI {

	public ConfirmationGUI(ProxiedPlayer player, ProxyRunnable disband, ALoreBuilder yesLore, ALoreBuilder noLore, String name) {
		super(player);
		setHomePanel(new ConfirmationPanel(this, disband, yesLore, noLore, name));
	}
}
