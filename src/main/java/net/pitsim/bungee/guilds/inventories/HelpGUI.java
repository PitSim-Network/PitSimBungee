package net.pitsim.bungee.guilds.inventories;

import net.pitsim.bungee.misc.PreparedGUI;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class HelpGUI extends PreparedGUI {

	public HelpGUI(ProxiedPlayer player) {
		super(player);

		setHomePanel(new HelpPanel(this));
	}
}
