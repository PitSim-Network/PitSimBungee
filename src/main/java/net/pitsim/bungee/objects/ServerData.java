package net.pitsim.bungee.objects;


import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ServerData {

	private List<String> data;
	private List<String> playerStrings;

	public ServerData(List<String> data) {
		this.data = data;
		this.playerStrings = new ArrayList<>();

		for(int i = 0; i < data.size(); i++) {
			playerStrings.add(ChatColor.translateAlternateColorCodes('&', data.get(i)));
		}
	}

	public List<String> getData() {
		return data;
	}

	public List<String> getPlayerStrings() {
		return playerStrings;
	}



}
