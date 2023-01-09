package dev.wiji.instancemanager.market;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarketListing {

	private UUID auctionUUID;
	private UUID ownerUUID;
	private long creationTime;

	private ListingType type;

	private Map<UUID, Integer> bidMap;
	private int startingBid;

	private int binPrice;

	public MarketListing(UUID ownerUUID, ListingType type, int startingBid, int binPrice) {
		this.ownerUUID = ownerUUID;
		this.type = type;
		this.startingBid = startingBid;
		this.binPrice = binPrice;

		this.auctionUUID = UUID.randomUUID();
		this.creationTime = System.currentTimeMillis();
		this.bidMap = new HashMap<>();
	}

	public MarketListing() {

	}

	public void save() {
		try {
			String json = MarketManager.gson.toJson(this);
			FileWriter writer = new FileWriter(MarketManager.getListingFile(this));
			writer.write(json);
			writer.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public UUID getUUID() {
		return auctionUUID;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public ListingType getType() {
		return type;
	}

	public Map<UUID, Integer> getBidMap() {
		return bidMap;
	}

	public int getStartingBid() {
		return startingBid;
	}

	public int getBinPrice() {
		return binPrice;
	}
}

enum ListingType {
	BIN,
	AUCTION,
	BIN_AUCTION,
	STACK_BIN;
}
