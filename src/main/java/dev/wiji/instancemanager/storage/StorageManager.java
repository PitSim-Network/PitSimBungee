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
	public static final int MAX_ENDERCHEST_PAGES = 18;
	public static final int ENDERCHEST_PAGE_SLOTS = 36;

	public static List<StorageProfile> profiles = new ArrayList<>();
	public static Gson gson = new Gson();

	protected static File getStorageFile(UUID uuid) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + uuid + ".json");
		file.getParentFile().mkdirs();
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
		for(StorageProfile profile : new ArrayList<>(profiles)) {
			if(profile == null) {
				continue;
			}

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
			strings.remove(0);
			String serverName = strings.remove(0);
			UUID uuid = UUID.fromString(strings.remove(0));
			boolean isLogout = message.getBooleans().remove(0);

			StorageProfile profile = getStorage(uuid);
			profile.updateData(message, serverName, isLogout);

			if(!isLogout) return;
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
			if(player == null) profiles.remove(profile);
		}
	}

	public static boolean isLoaded(StorageProfile profile) {
		return profiles.contains(profile);
	}
}
