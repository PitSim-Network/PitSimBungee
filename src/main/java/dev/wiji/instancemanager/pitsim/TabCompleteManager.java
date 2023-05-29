package dev.wiji.instancemanager.pitsim;

import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabCompleteManager implements Listener {

	@EventHandler
	public void onTabComplete(TabCompleteResponseEvent event) {
		event.getSuggestions().clear();
	}
}
