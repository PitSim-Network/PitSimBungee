package dev.wiji.instancemanager.objects;


import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ServerData {

	private List<String> data;
	private List<String> playerStrings;

	public ServerData(List<String> data) {
		this.data = data;
		this.playerStrings = new ArrayList<>();

		for(String playerString : data) {
			playerStrings.add(ChatColor.translateAlternateColorCodes('&', playerString));
		}
	}

	public List<String> getData() {
		return data;
	}

	public List<String> getPlayerStrings() {
		return playerStrings;
	}


}
