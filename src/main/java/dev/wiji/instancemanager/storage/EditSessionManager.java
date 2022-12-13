package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditSessionManager implements Listener {

	public static List<EditSession> editSessions = new ArrayList<>();

	public static void createSession(UUID staffUUID, String playerName) {
		ProxiedPlayer staff = BungeeMain.INSTANCE.getProxy().getPlayer(staffUUID);
		if(staff == null) return;

		UUID playerUUID;

		try {
			playerUUID = UUID.fromString(playerName);
		} catch (IllegalArgumentException e) {
			playerUUID = BungeeMain.getUUID(playerName, false);
		}

		if(playerUUID == null) {
			staff.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Player not found. (Try UUID?)"));
			return;
		}

		EditSession session = new EditSession(staff, playerUUID);
		editSessions.add(session);
	}

	public static boolean isBeingEdited(UUID playerUUID) {
		for(EditSession session : editSessions) {
			if(session.getPlayerUUID().equals(playerUUID)) return true;
		}
		return false;
	}
}
