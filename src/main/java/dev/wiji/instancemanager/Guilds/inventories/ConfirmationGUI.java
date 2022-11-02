package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ConfirmationGUI extends PreparedGUI {

	public ConfirmationGUI(ProxiedPlayer player, ProxyRunnable disband, ALoreBuilder yesLore, ALoreBuilder noLore, String name) {
		super(player);
		setHomePanel(new ConfirmationPanel(this, disband, yesLore, noLore, name));
	}
}
