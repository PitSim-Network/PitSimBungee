package dev.wiji.instancemanager.auctions;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class AuctionItem {
	public int slot;

	public long itemSeed;
	public long dataSeed;

	public Map<UUID, Integer> bidMap;

	public AuctionItem(int slot, long itemSeed, long dataSeed, Map<UUID, Integer> bidMap) {
		this.slot = slot;
		this.itemSeed = itemSeed;
		this.dataSeed = dataSeed;
		this.bidMap = bidMap;

		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.status.isOnline()) continue;

			sendDataToServer(server.getServerInfo().getName());
		}

		save();
	}

	public AuctionItem() {

	}

	public boolean placeBid(UUID player, int bid) {
		if(bid < getMinBid()) return false;

		bidMap.put(player, bid);
		save();
		sendBidsToServers();
		return true;
	}

	public void sendBidsToServers() {
		PluginMessage message = new PluginMessage();
		message.writeString("AUCTION BID DATA");
		message.writeInt(slot);

		message.writeString(getBidString());
		message.writeString(getNameString());
		PitSimServerManager.mixedServerList.forEach(server -> {
			if(server.status.isOnline()) message.addServer(server.getServerInfo());
		});

		message.send();
	}

	public void sendDataToServer(String server) {
		PluginMessage message = new PluginMessage();
		message.writeString("AUCTION DATA");
		message.writeLong(itemSeed).writeLong(dataSeed).writeLong(AuctionManager.endTime);
		message.writeInt(slot);
		message.writeString(getBidString()).writeString(getNameString());
		message.addServer(server);
		message.send();
	}

	public void save() {
		try {
			String json = AuctionManager.gson.toJson(this);
			FileWriter writer = new FileWriter(AuctionManager.getSaveFile(slot));
			writer.write(json);
			writer.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void end() {
		UUID winner = getHighestBidder();

		for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
			UUID uuid = entry.getKey();
			int bid = entry.getValue();

			if(uuid.equals(winner)) continue;
			new AuctionRewardManager.AuctionSoulReturn(uuid, bid);
		}
		AuctionManager.auctionRewardManager.saveRewards();

		if(winner == null) return;
		new AuctionRewardManager.AuctionItemReward(winner, itemSeed, dataSeed);
		AuctionManager.auctionRewardManager.saveRewards();

		PluginMessage message = new PluginMessage().writeString("AUCTION ANNOUNCEMENT");
		String playerName = Misc.getRankColor(winner) + BungeeMain.getName(winner, false);

		message.writeString("&5&lDARK AUCTION! " + playerName + " &7has won %item% &7for &f" + getHighestBid() + " Souls&7!");
		message.writeLong(itemSeed);

		PitSimServerManager.mixedServerList.forEach(server -> {
			if(server.status.isOnline()) message.addServer(server.getServerInfo());
		});

		message.send();
	}

	public int getHighestBid() {
		int highest = 0;
		for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
			if(entry.getValue() > highest) {
				highest = entry.getValue();
			}
		}
		return highest;
	}

	public UUID getHighestBidder() {
		int highest = 0;
		UUID player = null;
		for(Map.Entry<UUID, Integer> entry : bidMap.entrySet()) {
			if(entry.getValue() > highest) {
				highest = entry.getValue();
				player = entry.getKey();
			}
		}
		return player;
	}

	public int getMinBid() {
		int currentBid = getHighestBid();

		if(getHighestBidder() == null) return currentBid;

		int bid = (int) Math.ceil(currentBid + (currentBid * 0.1));
		return Math.max(1, bid);
	}

	public String getBidString() {
		StringBuilder bidData = new StringBuilder();
		for(Map.Entry<UUID, Integer> uuidIntegerEntry : bidMap.entrySet()) {
			bidData.append(uuidIntegerEntry.getKey().toString()).append(":").append(uuidIntegerEntry.getValue());
			if(uuidIntegerEntry != bidMap.entrySet().toArray()[bidMap.size() - 1]) {
				bidData.append(",");
			}
		}
		return bidData.toString();
	}

	public String getNameString() {
		StringBuilder nameData = new StringBuilder();
		for(UUID uuid : bidMap.keySet()) {
			String name = BungeeMain.getName(uuid, false);
			nameData.append(uuid.toString()).append(":").append(name);
			if(uuid != bidMap.keySet().toArray()[bidMap.size() - 1]) {
				nameData.append(",");
			}
		}
		return nameData.toString();
	}
}
