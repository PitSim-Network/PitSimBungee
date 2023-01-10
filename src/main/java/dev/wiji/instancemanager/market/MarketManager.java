package dev.wiji.instancemanager.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketManager {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	List<MarketListing> listings = new ArrayList<>();

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
