package dev.wiji.instancemanager.market;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.alogging.LogType;
import dev.wiji.instancemanager.discord.MarketLog;
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
	private boolean availableForPurchase = true;
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

			if(j < bidMap.size() - 1) builder.append(",");

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

		String message = "&a&lMARKET " + getDisplayName(playerUUID) + " &7has the top bid of &f" + bidAmount + " Souls &7on "
				+ CustomSerializer.deserialize(itemData).displayName + "&7!";
		if(getHighestBidder() != null) {
			new MarketAlertManager.MarketAlert(getHighestBidder(), marketUUID, message);
		}
		MarketManager.replaceAlerts(ownerUUID, marketUUID, message);


		if(getTimeLeft() < (1000 * 60 * 2)) setTime();

		bidMap.put(playerUUID, bidAmount);

		if(binPrice != -1 && bidAmount >= binPrice) {
			bin(playerUUID, 1, true);

			String binMessage = "&a&lMARKET &7Since your bid went above the listing's BIN price, you have won the auction!";
			new MarketAlertManager.MarketAlert(playerUUID, marketUUID, binMessage);

			return;
		}

		MarketManager.sendSuccess(playerUUID, this);
		MarketLog.log(this, LogType.MARKET_BID, playerUUID, new int[] {bidAmount});

		buyer = playerUUID;
		buyerDisplayName = getDisplayName(playerUUID);
		claimableSouls = bidAmount;

		update();
	}

	public void bin(UUID playerUUID, int amount, boolean holdItem) {

		if(binPrice == -1) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(!availableForPurchase) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		CustomSerializer.LimitedItemStack stack = CustomSerializer.deserialize(itemData);

		if(stackBIN) {
			int stock = stack.amount;

			if(amount > stock) {
				MarketManager.sendFailure(playerUUID, this);
				return;
			}

			stock -= amount;
			MarketManager.sendSuccess(playerUUID, this);
			MarketLog.log(this, LogType.MARKET_BIN, playerUUID, new int[] {binPrice * amount, amount});
			stack.amount = stock;
			itemData = CustomSerializer.serialize(stack);
			claimableSouls += (binPrice * amount);

			String message = "&a&lMARKET " + getDisplayName(playerUUID) + " &7has purchased &8" + amount + "x " + stack.displayName + " &7for &f" + (binPrice * amount) + " Souls&7!";
			new MarketAlertManager.MarketAlert(ownerUUID, marketUUID, message);

			if(stock == 0) {
				itemClaimed = true;
				end();
			} else update();

		} else {
			claimableSouls += binPrice;
			if(!holdItem) itemClaimed = true;
			buyer = playerUUID;
			availableForPurchase = false;
			buyerDisplayName = getDisplayName(playerUUID);


			String message = "&a&lMARKET " + getDisplayName(playerUUID) + " &7has purchased " + stack.displayName + " &7for &f" + (binPrice) + " Souls&7!";
			new MarketAlertManager.MarketAlert(ownerUUID, marketUUID, message);

			MarketManager.sendSuccess(playerUUID, this);
			MarketLog.log(this, LogType.MARKET_BIN, playerUUID, new int[] {binPrice * amount, amount});
			end();
		}
	}

	public void claimItem(UUID playerUUID) {
		if(itemClaimed) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(!playerUUID.equals(ownerUUID)) {
			if(startingBid == -1 || !hasEnded) {
				MarketManager.sendFailure(playerUUID, this);
				return;
			}
		}

		if(getHighestBidder() != null && playerUUID.equals(getHighestBidder())) {
			MarketManager.sendSuccess(playerUUID, this);
			MarketLog.log(this, LogType.MARKET_CLAIM_ITEM, playerUUID, new int[] {});
			itemClaimed = true;
			update();
		} else if(playerUUID.equals(ownerUUID) && hasEnded) {
			MarketManager.sendSuccess(playerUUID, this);
			MarketLog.log(this, LogType.MARKET_CLAIM_ITEM, playerUUID, new int[] {});
			itemClaimed = true;
			update();
		}

		if(itemClaimed && claimableSouls == 0 && bidMap.size() <= 1) {
			remove();
		}

		if(!itemClaimed) {
			MarketManager.sendFailure(playerUUID, this);
		}
	}

	public void claimSouls(UUID playerUUID) {
		if(!hasEnded) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(playerUUID.equals(buyer)) {
			MarketManager.sendFailure(playerUUID, this);
			return;
		}

		if(bidMap.containsKey(playerUUID)) {
			MarketLog.log(this, LogType.MARKET_CLAIM_SOULS, playerUUID, new int[] {bidMap.get(playerUUID)});
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
		MarketLog.log(this, LogType.MARKET_CLAIM_SOULS, playerUUID, new int[] {claimableSouls});
		claimableSouls = 0;

		if(itemClaimed && bidMap.size() <= 1) remove();
		else update();
	}

	public void end() {

		CustomSerializer.LimitedItemStack item = CustomSerializer.deserialize(itemData);

		if(startingBid != -1 && isExpired()) {

			if(bidMap.isEmpty()) {
				String ownerMessage = "&a&lMARKET &7Your " + item.displayName + " &7has expired with no bids!";
				MarketManager.replaceAlerts(ownerUUID, marketUUID, ownerMessage);
			} else {
				for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
					String message;
					if(entry.getKey().equals(getHighestBidder()))
						message = "&a&lMARKET &7You won " + item.displayName + " &7for &f" + getHighestBid() + " Souls&7!";
					else
						message = "&a&lMARKET &7The auction for " + item.displayName + " &7has ended. Visit the market to reclaim &f" + getHighestBid() + " Souls&7!";
					MarketManager.replaceAlerts(entry.getKey(), marketUUID, message);
				}
			}

			if(getHighestBidder() != null) {
				String message = "&a&lMARKET &7You won " + item.displayName + " &7for &f" + getHighestBid() + " Souls&7!";
				MarketManager.replaceAlerts(getHighestBidder(), marketUUID, message);

				String ownerMessage = "&a&lMARKET " + getDisplayName(getHighestBidder()) + " &7Bought your " + item.displayName + " &7for &f" + getHighestBid() + " Souls&7!";
				MarketManager.replaceAlerts(ownerUUID, marketUUID, ownerMessage);
			}
		} else if(stackBIN) {
			String ownerMessage = "&a&lMARKET &7Your listing for " + item.displayName + " &7has expired with &a" + item.amount + " Items &7remaining!";
			new MarketAlertManager.MarketAlert(ownerUUID, marketUUID, ownerMessage);
		} else {
			String ownerMessage = "&a&lMARKET &7Your listing for " + item.displayName + " &7has expired!";
			MarketManager.replaceAlerts(ownerUUID, marketUUID, ownerMessage);
		}

		hasEnded = true;
		MarketLog.log(this, LogType.MARKET_END, null, new int[] {});
		update();
	}

	public void staffEnd(UUID staffUUID) {

		CustomSerializer.LimitedItemStack stack = CustomSerializer.deserialize(itemData);
		String alert = "&a&lMARKET &7Your " + stack.displayName + " &7listing has been forcibly ended. " +
				"&cYou will not receive back the item or any souls. Make a ticket in our Discord if you believe this was an error.";
		MarketManager.replaceAlerts(ownerUUID, marketUUID, alert);

		if(startingBid != -1) {
			bidMap.put(staffUUID, Integer.MAX_VALUE);
			buyer = staffUUID;
			claimableSouls = 0;
			hasEnded = true;

			for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
				if(entry.getKey().equals(staffUUID)) continue;

				String message = "&a&lMARKET &7Your have been refunded &f" + entry.getValue() + " Souls &7 since" +
						" an auction has been forcibly ended. Visit the market to claim them!";
				MarketManager.replaceAlerts(entry.getKey(), marketUUID, message);
			}

			update();
			MarketManager.sendSuccess(staffUUID, this);
			MarketLog.log(this, LogType.MARKET_STAFF_END, staffUUID, new int[] {});
		} else {
			MarketManager.sendSuccess(staffUUID, this);
			MarketLog.log(this, LogType.SERVER_START, staffUUID, new int[] {binPrice});
			remove();
		}
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

	public long getTimeLeft() {
		return (creationTime + listingLength) - System.currentTimeMillis();
	}

	public void setTime() {
		this.listingLength += (1000 * 60 * 2) - getTimeLeft();
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

	public CustomSerializer.LimitedItemStack getDeserializedData() {
		return CustomSerializer.deserialize(itemData);
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

	public long getListingLength() {
		return listingLength;
	}

	public UUID getBuyer() {
		return buyer;
	}

	public int getOriginalStock() {
		return originalStock;
	}
}


