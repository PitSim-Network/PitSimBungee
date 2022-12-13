package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class EditSession {

	private final ProxiedPlayer staffPlayer;
	private final UUID playerUUID;
	private final StorageProfile editProfile;

	public EditSession(ProxiedPlayer staffPlayer, UUID playerUUID) {
		this.staffPlayer = staffPlayer;
		this.playerUUID = playerUUID;

		if(!StorageManager.hasStorageFile(playerUUID)) {
			AOutput.error(staffPlayer, "Player has never joined!");
			endSession();
		}

		editProfile = StorageManager.getStorage(playerUUID);

		ServerInfo serverInfo = staffPlayer.getServer().getInfo();
		editProfile.sendToServer(serverInfo);

		PluginMessage editMessage = new PluginMessage().writeString("EDIT SESSION").writeString(staffPlayer.getUniqueId().toString());
		editMessage.writeString(playerUUID.toString()).send();

		//TODO: Ensure that the playerdata persists on the server if the player leaves
		//TODO: Make sure the editor's playerdata cannot save during the session
		//TODO: Make sure you cannot edit the playerdata of a player who is editing someone else
		//TODO: If the player leaves the server, end the session, don't save the data of the editor, and dont save the updates to the players inventory

	}

	public void endSession() {
		EditSessionManager.editSessions.remove(this);
	}

	public ProxiedPlayer getStaffPlayer() {
		return staffPlayer;
	}

	public StorageProfile getEditProfile() {
		return editProfile;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}
}
