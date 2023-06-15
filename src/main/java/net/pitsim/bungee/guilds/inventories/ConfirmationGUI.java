package net.pitsim.bungee.guilds.inventories;

import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.PreparedGUI;
import net.pitsim.bungee.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ConfirmationGUI extends PreparedGUI {

	public ConfirmationGUI(ProxiedPlayer player, ProxyRunnable disband, ALoreBuilder yesLore, ALoreBuilder noLore, String name) {
		super(player);
		setHomePanel(new ConfirmationPanel(this, disband, yesLore, noLore, name));
	}
}
