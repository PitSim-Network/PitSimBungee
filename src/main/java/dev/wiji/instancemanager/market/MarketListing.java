package dev.wiji.instancemanager.market;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.misc.CustomSerializer;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;
import net.luckperms.api.model.user.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

	private boolean hasEnded = false;
	private int originalStock;
	private UUID buyer = null;
	private String buyerDisplayName = "&cNONE";

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
		this.originalStock = stackBIN ? CustomSerializer.deserialize(itemData).amount : 1;
	}

	public MarketListing() {

	}

	public void update() {
		PluginMessage message = new PluginMessage().writeString("MARKET UPDATE").writeString(marketUUID.toString());
		message.writeString(ownerUUID.toString());
		message.writeInt(startingBid);
		message.writeInt(binPrice);
		message.writeBoolean(stackBIN);
		message.writeString(itemData);
		message.writeLong(listingLength);
		message.writeLong(creationTime);
		message.writeInt(claimableSouls);
		message.writeBoolean(itemClaimed);
		message.writeBoolean(hasEnded);
		message.writeString(getDisplayName(ownerUUID));
		message.writeString(buyer == null ? "" : buyer.toString());
		message.writeString(buyerDisplayName);

		StringBuilder bidMapBuilder = new StringBuilder();

		int i = 0;
		for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
			bidMapBuilder.append(entry.getKey()).append(":").append(entry.getValue());
			if(i < bidMap.size() - 1) bidMapBuilder.append(",");

			i++;
		}
		message.writeString(bidMapBuilder.toString());

		StringBuilder builder = new StringBuilder();
		int j = 0;
		for(UUID uuid : bidMap.keySet()) {
			builder.append(uuid.toString() + ":" + getDisplayName(uuid));

			if(j < bidMap.size() - 1) bidMapBuilder.append(",");

			j++;
		}
		message.writeString(builder.toString());
		message.writeInt(originalStock);


		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(!darkzoneServer.status.isOnline()) continue;
			message.addServer(darkzoneServer.getServerInfo());
		}

		message.send();

		save();
	}

	public void placeBid(UUID playerUUID, int bidAmount) {
		if(startingBid == -1 || isExpired() || bidAmount < getMinimumBid() || stackBIN) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(binPrice != -1 && bidAmount >= binPrice) {
			bin(playerUUID, 1);
			return;
		}

		bidMap.put(playerUUID, bidAmount);
		MarketManager.sendSuccess(playerUUID, this);

		buyer = playerUUID;
		buyerDisplayName = getDisplayName(playerUUID);
		claimableSouls = bidAmount;

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

			if(stock == 0) {
				itemClaimed = true;
				end();
			}
			else update();

		} else {
			claimableSouls += binPrice;
			itemClaimed = true;
			buyer = playerUUID;
			buyerDisplayName = getDisplayName(playerUUID);
			MarketManager.sendSuccess(playerUUID, this);
			end();
		}
	}

	public void claimItem(UUID playerUUID) {
		if(itemClaimed) {
			MarketManager.sendFailure(playerUUID, this);
			System.out.println("Failure 1");
			return;
		}

		if(!playerUUID.equals(ownerUUID)) {
			if(startingBid == -1 || !isExpired()) {
				MarketManager.sendFailure(playerUUID, this);
				System.out.println("Failure 2");
				return;
			}
		}

		if(getHighestBidder() != null && playerUUID.equals(getHighestBidder())) {
			MarketManager.sendSuccess(playerUUID, this);
			itemClaimed = true;
			update();
		} else if(playerUUID.equals(ownerUUID) && isExpired()) {
			MarketManager.sendSuccess(playerUUID, this);
			itemClaimed = true;
			update();
		}

		if(itemClaimed && claimableSouls == 0 && bidMap.size() <= 1) {
			remove();
		}

		if(!itemClaimed) MarketManager.sendFailure(playerUUID, this);
	}

	public void claimSouls(UUID playerUUID) {

		if(playerUUID.equals(buyer)) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(bidMap.containsKey(playerUUID)) {
			bidMap.remove(playerUUID);
			MarketManager.sendSuccess(playerUUID, this);
			update();
			return;
		}

		if(!playerUUID.equals(ownerUUID) || claimableSouls == 0) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		MarketManager.sendSuccess(playerUUID, this);
		claimableSouls = 0;

		if(itemClaimed && bidMap.size() <= 1) remove();
		else update();
	}

	public void end() {
		hasEnded = true;
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

		MarketManager.getListingFile(this).delete();
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
		return (int) Math.max(getHighestBid() * 1.2, startingBid);
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

	public String getDisplayName(UUID player) {
		User user;
		try {
			user = BungeeMain.LUCKPERMS.getUserManager().loadUser(player).get();
			return user.getCachedData().getMetaData().getPrefix() + BungeeMain.getName(player, false);
		} catch(InterruptedException | ExecutionException exception) {
			exception.printStackTrace();
			return "&cERROR";
		}
	}

	public boolean isExpired() {
		return creationTime + listingLength <= System.currentTimeMillis();
	}

	public boolean isEnded() {
		return hasEnded;
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

	public String getItemData() {
		return itemData;
	}

	public int getOriginalStock() {
		return originalStock;
	}
}

