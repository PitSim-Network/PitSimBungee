package dev.wiji.instancemanager.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MarketManager {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static List<MarketListing> listings = new ArrayList<>();

	static {
		((ProxyRunnable) () -> {
			for(MarketListing listing : listings) {
				if(listing.isEnded()) continue;
				if(listing.isExpired()) listing.end();
			}
		}).runAfterEvery(1, 1, TimeUnit.SECONDS);
	}

	public static void init() {
		File folder = new File(BungeeMain.INSTANCE.getDataFolder() + "/market/");
		File[] fileArray = folder.listFiles();

		assert fileArray != null;
		for(File file : fileArray) {
			loadListing(file);
		}
	}

	public static void shutdown() {
		for(MarketListing listing : listings) {
			listing.save();
		}
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

	public static File getListingFile(MarketListing listing) {
		return new File(BungeeMain.INSTANCE.getDataFolder() + "/market/" + listing.getUUID() + ".json");
	}

	public static void sendFailure(UUID playerUUID, MarketListing listing) {

	}

	public static void sendFailure(UUID playerUUID, UUID listingID) {

	}

	public static void sendSuccess(UUID playerUUID, MarketListing listing) {

	}
}
