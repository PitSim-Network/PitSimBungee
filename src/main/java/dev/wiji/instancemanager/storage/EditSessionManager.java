package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import org.bukkit.event.EventHandler;

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

		if(isBeingEdited(playerUUID)) {
			staff.sendMessage(new TextComponent(ChatColor.RED + "That player is already being edited!"));
			return;
		}

		for(EditSession editSession : editSessions) {
			if(editSession.getStaffUUID().equals(staffUUID)) {
				staff.sendMessage(new TextComponent(ChatColor.RED + "You are already editing a player!"));
				return;
			}
		}

		if(playerUUID == null) {
			staff.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Player not found. (Try UUID?)"));
			return;
		}

		EditSession session = new EditSession(staffUUID, playerUUID);
		editSessions.add(session);
	}

	public static boolean isBeingEdited(UUID playerUUID) {
		for(EditSession session : editSessions) {
			if(session.getPlayerUUID().equals(playerUUID) && session.isActive) return true;
		}
		return false;
	}

	@EventHandler
	public void onLogout(PlayerDisconnectEvent event) {
		for(EditSession session : editSessions) {
			if(session.getStaffUUID().equals(event.getPlayer().getUniqueId())) {
				session.endSession();
			}
		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();

		if(strings.get(0).equals("EDIT RESPONSE")) {
			UUID uuid = UUID.fromString(strings.get(1));

			for(EditSession session : editSessions) {
				if(session.getStaffUUID().equals(uuid)) {
					strings.remove(0);
					strings.remove(0);
					session.receivePromptResponse(event.getMessage());
				}
			}
		}
	}

}
