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

		StorageProfile profile = gson.fromJson(getStorageFile(player).toString(), StorageProfile.class);
		profile.init(player);
		profiles.add(profile);
		return profile;
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.get(0).equals("ENDERCHEST")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(strings.get(1));
			if(player == null) return;

			StorageProfile profile = getStorage(player);

			strings.remove(0);
			strings.remove(0);
			profile.updateEnderchest(message);
		}

		if(strings.get(0).equals("INVENTORY")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(strings.get(1));
			if(player == null) return;

			StorageProfile profile = getStorage(player);

			strings.remove(0);
			strings.remove(0);
			profile.updateInventory(message);
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();

		StorageProfile profile = getStorage(player);

		profile.save();
		profiles.remove(profile);
	}


}
