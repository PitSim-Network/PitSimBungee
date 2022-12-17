package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EditSession {

	private final UUID staffUUID;
	private final ProxiedPlayer staffPlayer;
	private final UUID playerUUID;
	private final StorageProfile editProfile;

	public boolean isActive;
	ScheduledTask timeoutTask;

	public EditSession(UUID staffUUID, UUID playerUUID) {
		this.staffUUID = staffUUID;
		this.playerUUID = playerUUID;

		this.staffPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(staffUUID);

		isActive = false;

		if(staffPlayer == null) {
			endSession();
		}

		if(!StorageManager.hasStorageFile(playerUUID)) {
			AOutput.error(staffPlayer, "Player has never joined!");
			endSession();
		}

		prompt();
		editProfile = StorageManager.getStorage(playerUUID);

		//TODO: Ensure that the playerdata persists on the server if the player leaves
		//TODO: Make sure the editor's playerdata cannot save during the session
		//TODO: Make sure you cannot edit the playerdata of a player who is editing someone else
		//TODO: If the player leaves the server, end the session, don't save the data of the editor, and dont save the updates to the players inventory

	}

	public void receivePromptResponse(PluginMessage message) {
		List<String> strings = message.getStrings();
		List<Boolean> booleans = message.getBooleans();

		System.out.println("Test2");

		timeoutTask.cancel();
		timeoutTask = null;

		if(strings.get(0).equals("ONLINE") || strings.get(0).equals("CANCEL")) {
			endSession();
		} else if(strings.get(0).equals("OFFLINE")) {
			isActive = true;
			editProfile.sendToServer(getStaffServer());

			boolean kick = booleans.get(0);
			if(kick && playerIsInPitsim()) {
				ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
				if(player != null) {
					player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "Your data is being modified. Please rejoin in a moment."));
				}
			}
		}
	}

	public void prompt() {
		PluginMessage message = new PluginMessage().writeString("PROMPT EDIT MENU");
		message.writeString(staffPlayer.getUniqueId().toString()).send();
		message.writeString(playerUUID.toString());
		message.writeBoolean(getPlayerServer() != null);
		message.writeString(getPlayerServer() == null ? "" : getPlayerServer().getName());

		timeoutTask = ((ProxyRunnable) this::endSession).runAfter(10, TimeUnit.SECONDS);

		message.addServer(getStaffServer());
		message.send();
	}

	public void endSession() {
		EditSessionManager.editSessions.remove(this);
	}

	public ServerInfo getPlayerServer() {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
		if(player == null) return null;
		return player.getServer().getInfo();
	}

	public ServerInfo getStaffServer() {
		return staffPlayer.getServer().getInfo();
	}


	public UUID getStaffUUID() {
		return staffUUID;
	}

	public boolean playerIsInPitsim() {
		return getPlayerServer().getName().contains("pitsim") || getPlayerServer().getName().contains("darkzone");
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
