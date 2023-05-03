package dev.wiji.instancemanager.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MarketManager implements Listener {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static List<MarketListing> listings = new ArrayList<>();
	public static MarketAlertManager marketAlertManager;

	public static File alertDataFile;


	static {
		((ProxyRunnable) () -> {
			for(MarketListing listing : listings) {
				if(listing.isEnded()) continue;
				if(listing.isExpired()) {
					//TODO: Send message to owner, bidders, and winner
					listing.end();
				}
			}
		}).runAfterEvery(1, 1, TimeUnit.SECONDS);
	}

	public static void init() {
		File folder = new File(BungeeMain.INSTANCE.getDataFolder() + "/market/");
		if(!folder.exists()) folder.mkdirs();
		File[] fileArray = folder.listFiles();

		assert fileArray != null;
		for(File file : fileArray) {
			loadListing(file);
		}

		try {
			alertDataFile = new File(BungeeMain.INSTANCE.getDataFolder() + "/market-alerts.json");
			if(!alertDataFile.exists()) alertDataFile.createNewFile();
		} catch(IOException exception) {
			throw new RuntimeException(exception);
		}

		Reader reader = null;
		try {
			reader = Files.newBufferedReader(alertDataFile.toPath());
		} catch(IOException e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		assert reader != null;
		marketAlertManager = gson.fromJson(reader, MarketAlertManager.class);
		if(marketAlertManager == null) marketAlertManager = new MarketAlertManager();
		System.out.println(marketAlertManager);
	}

	public static void shutdown() {
		for(MarketListing listing : listings) {
			listing.save();
		}

		marketAlertManager.save();
	}

	public static void loadListing(File file) {
		try {
			Reader reader = Files.newBufferedReader(file.toPath());
			listings.add(gson.fromJson(reader, MarketListing.class));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static MarketListing getListing(UUID uuid) {
		for(MarketListing listing : listings) {
			if(listing.getUUID().equals(uuid)) return listing;
		}
		return null;
	}

	public static void updateAll() {
		for(MarketListing listing : listings) {
			listing.update();
		}
	}

	public static File getListingFile(MarketListing listing) {
		return new File(BungeeMain.INSTANCE.getDataFolder() + "/market/" + listing.getUUID() + ".json");
	}

	public static void sendFailure(UUID playerUUID, MarketListing listing) {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
		if(player == null) return;

		PluginMessage message = new PluginMessage().writeString("MARKET ASYNC").writeString(playerUUID.toString());
		message.writeString(listing.getUUID().toString()).writeBoolean(false);
		message.addServer(player.getServer().getInfo()).send();
	}

	public static void sendFailure(UUID playerUUID, UUID listingID) {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
		if(player == null) return;

		PluginMessage message = new PluginMessage().writeString("MARKET ASYNC").writeString(playerUUID.toString());

		message.writeString(listingID.toString()).writeBoolean(false);
		message.addServer(player.getServer().getInfo()).send();
	}

	public static void sendSuccess(UUID playerUUID, MarketListing listing) {
		sendSuccess(playerUUID, listing.getUUID());
	}

	public static void sendSuccess(UUID playerUUID, UUID listingID) {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
		if(player == null) return;

		PluginMessage message = new PluginMessage().writeString("MARKET ASYNC").writeString(playerUUID.toString());
		message.writeString(listingID.toString()).writeBoolean(true);
		message.addServer(player.getServer().getInfo()).send();
	}

	public static List<MarketAlertManager.MarketAlert> getAlerts(UUID uuid) {
		List<MarketAlertManager.MarketAlert> marketAlerts = new ArrayList<>();
		for(MarketAlertManager.MarketAlert alert : marketAlertManager.alerts) {
			if(alert.playerUUID.equals(uuid)) marketAlerts.add(alert);
		}
		return marketAlerts;
	}

	public static List<MarketAlertManager.MarketAlert> getAlerts(UUID playerUUID, UUID marketUUID) {
		List<MarketAlertManager.MarketAlert> marketAlerts = new ArrayList<>();
		for(MarketAlertManager.MarketAlert alert : marketAlertManager.alerts) {
			if(alert.playerUUID.equals(playerUUID) && alert.listingID.equals(marketUUID)) marketAlerts.add(alert);
		}
		return marketAlerts;
	}

	public static void replaceAlerts(UUID playerUUID, UUID marketUUID, String message) {
		List<MarketAlertManager.MarketAlert> playerAlerts = getAlerts(playerUUID, marketUUID);
		marketAlertManager.alerts.removeAll(playerAlerts);
		new MarketAlertManager.MarketAlert(playerUUID, marketUUID, message);
	}

	public static void addAlert(MarketAlertManager.MarketAlert alert) {
		marketAlertManager.alerts.add(alert);
		marketAlertManager.save();
	}

	@EventHandler
	public void onJoin(ServerConnectedEvent event) {
		((ProxyRunnable) () -> {
			if(event.getPlayer() == null || !event.getPlayer().isConnected()) return;
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(pitSimServer.getServerInfo().equals(event.getServer().getInfo())) {

					List<MarketAlertManager.MarketAlert> remove = new ArrayList<>();
					for(MarketAlertManager.MarketAlert alert : getAlerts(event.getPlayer().getUniqueId())) {
						alert.send();
						remove.add(alert);
					}
					marketAlertManager.alerts.removeAll(remove);
					marketAlertManager.save();

					break;
				}
			}
		}).runAfter(1, TimeUnit.SECONDS);
	}
}
