package dev.wiji.instancemanager.market;

import dev.wiji.instancemanager.misc.CustomSerializer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarketListing {

	private UUID auctionUUID;
	private UUID ownerUUID;
	private long creationTime;

	private Map<UUID, Integer> bidMap;
	private String itemData;

	//Auction Data; -1 = Auction disabled
	private int startingBid = -1;
	//Bin Data; -1 = Bin disabled
	private int binPrice = -1;
	//Weather or not multiple items are being sold; Auction mode disabled by default if true
	private boolean stackBin;

	private transient AuctionStatus status;

	public MarketListing(UUID ownerUUID, String itemData, int startingBid, int binPrice, boolean stackBin) {
		this.ownerUUID = ownerUUID;
		this.startingBid = startingBid;
		this.binPrice = binPrice;
		this.stackBin = stackBin;

		this.auctionUUID = UUID.randomUUID();
		this.creationTime = System.currentTimeMillis();
		this.bidMap = new HashMap<>();
		status = AuctionStatus.ACTIVE;
	}

	public MarketListing() {

	}

	public void end(boolean bin) {
		status = AuctionStatus.ENDED;
	}

	public void placeBid(UUID playerUUID, int bidAmount) {
		if(startingBid == -1 || status == AuctionStatus.ENDED) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}


	}

	public void bin(UUID playerUUID, int amount) {
		if(stackBin) {
			CustomSerializer.LimitedItemStack stack = CustomSerializer.deserialize(itemData);
			int stock = stack.amount;

			if(amount > stock) {
				MarketManager.sendFailure(playerUUID, this);
				return;
			}

			stock-= amount;
			MarketManager.sendSuccess(playerUUID, this);
			stack.amount = stock;
			itemData = CustomSerializer.serialize(stack);

			if(stock == 0) {
				end(false);
			}
		} else {
			end(false);
		}
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

	public boolean isStackBin() {
		return stackBin;
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

	public AuctionStatus getStatus() {
		return status;
	}

	public enum AuctionStatus {
		ACTIVE,
		ENDED;
	}
}

