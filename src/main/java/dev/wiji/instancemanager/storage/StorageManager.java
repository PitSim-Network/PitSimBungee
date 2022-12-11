package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageManager implements Listener {

	public static List<StorageProfile> profiles = new ArrayList<>();
	public static Gson gson = new Gson();

	protected static File getStorageFile(UUID uuid) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + uuid + ".json");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
		return file;
	}

	public static StorageProfile getStorage(UUID uuid) {
		for(StorageProfile profile : profiles) {
			if(profile.getUUID().equals(uuid)) return profile;
		}

		StorageProfile profile;

		if(getStorageFile(uuid).length() == 0) {
			profile = new StorageProfile();
		} else {
			try {
				Reader reader = Files.newBufferedReader(getStorageFile(uuid).toPath());
				profile = gson.fromJson(reader, StorageProfile.class);
			} catch(Exception exception) {
				profile = new StorageProfile();
				exception.printStackTrace();
			}
	    }
		profile.init(uuid);

		profiles.add(profile);
		return profile;
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		if(strings.size() < 2) return;

		if(strings.get(0).equals("ITEM DATA SAVE")) {
			UUID uuid = UUID.fromString(strings.get(1));

			StorageProfile profile = getStorage(uuid);
			String server = strings.get(2);

			strings.remove(0);
			strings.remove(0);
			strings.remove(0);
			profile.updateData(message, server);
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();

		StorageProfile profile = getStorage(player.getUniqueId());

		profile.save();
		profiles.remove(profile);
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + event.getPlayer().getUniqueId() + ".json");
		if(file.exists()) return;

		if(!PitSimServerManager.serverList.get(0).status.isOnline()) {
			event.getPlayer().disconnect(TextComponent.fromLegacyText(ChatColor.RED + "We were unable to migrate your playerdata. Please report this issue."));
			return;
		}

		PluginMessage message = new PluginMessage().writeString("MIGRATE").writeString(event.getPlayer().getUniqueId().toString());
		message.addServer(PitSimServerManager.serverList.get(0).getServerInfo());
		message.send();
		StorageManager.loadPlayerData(event.getPlayer().getUniqueId());
	}

	public static void loadPlayerData(String playerName) {
		UUID uuid = BungeeMain.getUUID(playerName, false);

		assert uuid != null;
		PluginMessage message = new PluginMessage().writeString("LOAD REQUEST").writeString(uuid.toString());
		message.addServer("pitsim-1").send();
	}

	public static void loadPlayerData(UUID uuid) {

		assert uuid != null;
		PluginMessage message = new PluginMessage().writeString("LOAD REQUEST").writeString(uuid.toString());
		message.addServer("pitsim-1").send();
	}


}
