package net.pitsim.bungee.storage;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.events.MessageEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
		} catch(IllegalArgumentException e) {
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

		if(staff.getUniqueId().equals(playerUUID)) {
			staff.sendMessage(new TextComponent(ChatColor.RED + "You cannot edit your own profile!"));
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
		EditSession finalSession = null;

		for(EditSession session : editSessions) {
			if(session.getStaffUUID().equals(event.getPlayer().getUniqueId())) {
				finalSession = session;
			}
		}

		if(finalSession != null) finalSession.endSession();
	}

	@net.md_5.bungee.event.EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();

		if(strings.size() < 2) return;

		if(strings.get(0).equals("EDIT RESPONSE")) {

			UUID uuid = UUID.fromString(strings.get(1));

			for(EditSession session : editSessions) {
				if(session.getStaffUUID().equals(uuid)) {
					strings.remove(0);
					strings.remove(0);
					session.receivePromptResponse(event.getMessage());
					break;
				}
			}
		}

		if(strings.get(0).equals("EDIT SESSION END")) {
			UUID playerUUID = UUID.fromString(strings.get(1));

			EditSession endSession = null;

			for(EditSession editSession : editSessions) {
				if(editSession.getPlayerUUID().equals(playerUUID)) endSession = editSession;
			}

			if(endSession != null) endSession.endSession();
		}
	}

	public static EditSession getSession(UUID uuid) {
		for(EditSession session : editSessions) {
			if(session.getStaffUUID().equals(uuid)) return session;
		}
		return null;
	}

}
