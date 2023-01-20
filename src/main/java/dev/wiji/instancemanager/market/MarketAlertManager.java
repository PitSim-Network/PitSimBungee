package dev.wiji.instancemanager.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MarketAlertManager {

	public List<MarketAlert> alerts = new ArrayList<>();

	public MarketAlertManager() {
	}

	public static class MarketAlert {
		public UUID playerUUID;
		public UUID listingID;
		public String message;

		public MarketAlert(UUID playerUUID, UUID listingID, String message) {
			this.playerUUID = playerUUID;
			this.listingID = listingID;
			this.message = message;

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
			if(player != null) send();
			else MarketManager.addAlert(this);
		}

		public void send() {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
			if(player != null) player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
		}
	}

	public void save() {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(this);
			FileWriter writer = new FileWriter(MarketManager.alertDataFile.toPath().toString());
			writer.write(json);
			writer.close();
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}

}