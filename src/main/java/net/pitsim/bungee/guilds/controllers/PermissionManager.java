package net.pitsim.bungee.guilds.controllers;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class PermissionManager implements Listener {

	public static boolean isAdmin(ProxiedPlayer player) {
		return player.hasPermission("aguilds.admin");
	}
}
