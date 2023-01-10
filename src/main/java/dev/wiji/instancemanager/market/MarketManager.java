package dev.wiji.instancemanager.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
				if(listing.isEnded()) listing.end();
			}
		}).runAfterEvery(1, 1, TimeUnit.SECONDS);
	}

	public void loadListing(File file) {
		try {
			Reader reader = Files.newBufferedReader(file.toPath());
			listings.add(gson.fromJson(reader, MarketListing.class));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getListingFile(MarketListing listing) {
		return new File("market/" + listing.getUUID() + ".json");
	}

	public static void sendFailure(UUID playerUUID, MarketListing listing) {

	}

	public static void sendSuccess(UUID playerUUID, MarketListing listing) {

	}
}
