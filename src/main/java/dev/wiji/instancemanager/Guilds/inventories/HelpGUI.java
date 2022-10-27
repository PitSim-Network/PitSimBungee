package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Misc.PreparedGUI;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class HelpGUI extends PreparedGUI {

	public HelpGUI(ProxiedPlayer player) {
		super(player);

		setHomePanel(new HelpPanel(this));
	}
}
