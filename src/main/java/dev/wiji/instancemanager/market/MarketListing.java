package dev.wiji.instancemanager.market;

import dev.wiji.instancemanager.misc.Base64;
import dev.wiji.instancemanager.misc.CustomSerializer;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarketListing implements Serializable {

	private UUID marketUUID;
	private UUID ownerUUID;
	private long creationTime;

	private long listingLength;

	private Map<UUID, Integer> bidMap;
	private String itemData;

	//Auction Data; -1 = Auction disabled
	private int startingBid = -1;
	//Bin Data; -1 = Bin disabled
	private int binPrice = -1;
	//Weather or not multiple items are being sold; Auction mode disabled by default if true
	private boolean stackBIN;

	private int claimableSouls = 0;
	private boolean itemClaimed = false;

	public MarketListing(UUID ownerUUID, String itemData, int startingBid, int binPrice, boolean stackBIN, long listingLength) {
		this.ownerUUID = ownerUUID;
		this.startingBid = startingBid;
		this.binPrice = binPrice;
		this.stackBIN = stackBIN;
		this.itemData = itemData;
		this.listingLength = listingLength;
		this.marketUUID = UUID.randomUUID();
		this.creationTime = System.currentTimeMillis();
		this.bidMap = new HashMap<>();
	}

	public MarketListing() {

	}

	public void placeBid(UUID playerUUID, int bidAmount) {
		if(startingBid == -1 || isEnded() || bidAmount < getMinimumBid() || stackBIN) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(binPrice != -1 && bidAmount >= binPrice) {
			bin(playerUUID, 1);
			return;
		}

		bidMap.put(playerUUID, bidAmount);
		MarketManager.sendSuccess(playerUUID, this);

		update();
	}

	public void bin(UUID playerUUID, int amount) {
		if(binPrice == -1) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(stackBIN) {
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
			claimableSouls += (binPrice * amount);

			if(stock == 0) end();
			else update();

		} else {
			claimableSouls += binPrice;
			MarketManager.sendSuccess(playerUUID, this);
			end();
		}
	}

	public void claimItem(UUID playerUUID) {
		if(startingBid == -1 || !playerUUID.equals(getHighestBidder()) || !isEnded()) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		MarketManager.sendSuccess(playerUUID, this);
	}

	public void claimSouls(UUID playerUUID) {
		if(!playerUUID.equals(ownerUUID) || claimableSouls == 0) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		MarketManager.sendSuccess(playerUUID, this);
		claimableSouls = 0;

		if(!stackBIN && itemClaimed) remove();
	}

	public void end() {
		update();
	}

	public void remove() {
		PluginMessage message = new PluginMessage().writeString("MARKET REMOVAL").writeString(marketUUID.toString());
		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(!darkzoneServer.status.isOnline()) continue;
			message.addServer(darkzoneServer.getServerInfo());
		}
		message.send();

		MarketManager.listings.remove(this);
	}

	public void update() {
		PluginMessage message = new PluginMessage().writeString("MARKET UPDATE").writeString(marketUUID.toString());
		message.writeString(Base64.serialize(this));
		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(!darkzoneServer.status.isOnline()) continue;
			message.addServer(darkzoneServer.getServerInfo());
		}
		message.send();
		save();
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

	public int getMinimumBid() {
		if(startingBid == -1) return -1;
		return Math.max(getHighestBid(), startingBid);
	}

	public int getHighestBid() {
		int highest = 0;
		for(Integer value : bidMap.values()) {
			if(value > highest) highest = value;
		}
		return highest;
	}

	public UUID getHighestBidder() {
		UUID bidder = null;
		int highestValue = 0;
		for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
			if(entry.getValue() > highestValue) {
				bidder = entry.getKey();
				highestValue = entry.getValue();
			}
		}
		return bidder;
	}

	public boolean isEnded() {
		return creationTime + listingLength <= System.currentTimeMillis();
	}

	public UUID getUUID() {
		return marketUUID;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public boolean isStackBIN() {
		return stackBIN;
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

	public int getClaimableSouls() {
		return claimableSouls;
	}

	public boolean isItemClaimed() {
		return itemClaimed;
	}
}

