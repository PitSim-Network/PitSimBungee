package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

	protected static boolean hasStorageFile(UUID uuid) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + uuid + ".json");
		return file.exists();
	}

	public static StorageProfile getStorage(UUID uuid) {
		for(StorageProfile profile : profiles) if(profile.getUUID().equals(uuid)) return profile;

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
			boolean logout = message.getBooleans().get(0);

			profile.updateData(message, server, logout);

			if(logout) {
				ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
				if(player == null) {
					profiles.remove(profile);
				}
			}
		}
	}

	public static boolean isLoaded(StorageProfile profile) {
		return profiles.contains(profile);
	}
}
