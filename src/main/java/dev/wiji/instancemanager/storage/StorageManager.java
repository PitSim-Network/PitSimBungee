package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

	protected static boolean hasStorageFile(UUID uuid) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + uuid + ".json");
		return file.exists();
	}

	public static StorageProfile getStorage(UUID uuid) {

		for(StorageProfile profile : profiles) {
			if(profile.getUUID().equals(uuid)) return profile;
		}

		System.out.println("Failed to fetch profile");

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

//	@EventHandler
//	public void onLeave(PlayerDisconnectEvent event) {
//		ProxiedPlayer player = event.getPlayer();
//
//		StorageProfile profile = getStorage(player.getUniqueId());
//
//
////		((ProxyRunnable) () -> {
////
////		}).runAfter(200, TimeUnit.MILLISECONDS);
//	}

//	@EventHandler
//	public void onJoin(PostLoginEvent event) {
//		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + event.getPlayer().getUniqueId() + ".json");
//		if(file.exists()) return;
//
//		if(!PitSimServerManager.serverList.get(0).status.isOnline()) {
//			event.getPlayer().disconnect(TextComponent.fromLegacyText(ChatColor.RED + "We were unable to migrate your playerdata. Please report this issue."));
//			return;
//		}
//
//		PluginMessage message = new PluginMessage().writeString("MIGRATE").writeString(event.getPlayer().getUniqueId().toString());
//		message.addServer(PitSimServerManager.serverList.get(0).getServerInfo());
//		message.send();
//	}

//	@EventHandler
//	public void onServerLeave(ServerDisconnectEvent event) {
//		ProxiedPlayer player = event.getPlayer();
//		ServerInfo server = event.getTarget();
//
//		if(MainServer.getServer(server) == null) return;
//		StorageProfile profile = getStorage(player.getUniqueId());
//		List<StorageProfile> profiles = Objects.requireNonNull(MainServer.getServer(server)).loadedProfiles;
//
//		if(!profiles.contains(profile)) {
//			//TODO: Critical Error
//			return;
//		}
//
//		profiles.remove(profile);
//	}

	public static boolean isLoaded(StorageProfile profile) {
		return profiles.contains(profile);
	}
}
