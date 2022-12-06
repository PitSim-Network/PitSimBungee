package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
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

	protected static File getStorageFile(UUID player) {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + player + ".json");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static StorageProfile getStorage(UUID player) {
		for(StorageProfile profile : profiles) {
			if(profile.getUuid() == player) return profile;
		}

		StorageProfile profile;
		System.out.println("Length: " + getStorageFile(player).length());

		if(getStorageFile(player).length() == 0) {
			profile = new StorageProfile();
		} else {

			try {
				Reader reader = Files.newBufferedReader(getStorageFile(player).toPath());
				profile = gson.fromJson(reader, StorageProfile.class);
			} catch(Exception e) {
				profile = new StorageProfile();
				e.printStackTrace();
				System.out.println("Retard code");


			}
	    }
		profile.init(player);

		profiles.add(profile);
		return profile;
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		if(strings.size() < 2) return;

		System.out.println(strings.get(0));
		if(message.getIntegers().size() > 0) System.out.println(message.getIntegers().get(0));

		if(strings.get(0).equals("ENDERCHEST")) {
			System.out.println("Enderchest data");
			UUID uuid = UUID.fromString(strings.get(1));

			StorageProfile profile = getStorage(uuid);
			String server = strings.get(2);

			strings.remove(0);
			strings.remove(0);
			strings.remove(0);
			profile.updateEnderchest(message, server);
		}

		if(strings.get(0).equals("INVENTORY")) {
			System.out.println("inv rec 0");
			UUID uuid = UUID.fromString(strings.get(1));
			System.out.println("inv rec 1");

			StorageProfile profile = getStorage(uuid);
			String server = strings.get(2);

			strings.remove(0);
			strings.remove(0);
			strings.remove(0);
			profile.updateInventory(message, server);
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();

		StorageProfile profile = getStorage(player.getUniqueId());

		profile.save();
		profiles.remove(profile);
	}

	public static void loadPlayerData(String playerName) {
		UUID uuid = BungeeMain.getUUID(playerName, false);

		System.out.println("Sent load request");
		assert uuid != null;
		PluginMessage message = new PluginMessage().writeString("LOAD REQUEST").writeString(uuid.toString());
		message.addServer("pitsim-1").send();
	}


}
