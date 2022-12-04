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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageManager implements Listener {

	public static List<StorageProfile> profiles = new ArrayList<>();
	public static Gson gson = new Gson();

	protected static File getStorageFile(ProxiedPlayer player) {
		return new File("plugins/PitSimInstanceManager/Storage/" + player.getUniqueId() + ".json");
	}

	public static StorageProfile getStorage(ProxiedPlayer player) {
		for(StorageProfile profile : profiles) {
			if(profile.getPlayer() == player) return profile;
		}

		StorageProfile profile;

		try {
			profile = gson.fromJson(getStorageFile(player).toString(), StorageProfile.class);
		} catch(Exception e) {
			profile = new StorageProfile();
		}
		profile.init(player);

		profiles.add(profile);
		return profile;
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() == 0) {
			System.out.println("Strings: " + strings.toString());
			System.out.println("Integer: " + message.getIntegers().toString());
			System.out.println("Booleans: " + message.getBooleans().toString());
			System.out.println(message.messageID);
		}

		System.out.println(strings.get(0));
		if(message.getIntegers().size() > 0) System.out.println(message.getIntegers().get(0));

		if(strings.get(0).equals("ENDERCHEST")) {
			System.out.println("Enderchest data");
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			System.out.println(player);

			if(player == null) return;

			StorageProfile profile = getStorage(player);
			String server = strings.get(2);

			strings.remove(0);
			strings.remove(0);
			strings.remove(0);
			profile.updateEnderchest(message, server);
		}

		if(strings.get(0).equals("INVENTORY")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(strings.get(1));
			if(player == null) return;

			StorageProfile profile = getStorage(player);
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

		StorageProfile profile = getStorage(player);

		profile.save();
		profiles.remove(profile);
	}

	public static void loadPlayerData(ProxiedPlayer player) {
		System.out.println("Sent load request");
		PluginMessage message = new PluginMessage().writeString("LOAD REQUEST").writeString(player.getUniqueId().toString());
		message.addServer("pitsim-1").send();
	}


}
