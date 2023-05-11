package dev.wiji.instancemanager.auctions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AuctionManager {

	public static final int AUCTION_NUM = 3;

	public static AuctionItem[] auctionItems = new AuctionItem[AUCTION_NUM];
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static AuctionRewardManager auctionRewardManager;
	public static long endTime;

	public static void init() {
		auctionRewardManager = new AuctionRewardManager();
		endTime = auctionRewardManager.endTime;

		for(int i = 0; i < AUCTION_NUM; i++) {
			loadAuction(i);
		}

		((ProxyRunnable) () -> {
			if(System.currentTimeMillis() > endTime) {
				endTime = generateEndTime();

				for(int i = 0; i < auctionItems.length; i++) {
					auctionItems[i].end();

					auctionItems[i] = new AuctionItem(i, generateSeed(), generateSeed(), new LinkedHashMap<>());
				}
			}
		}).runAfterEvery(1, 1, TimeUnit.SECONDS);
	}

	public static AuctionItem getAuctionItem(int slot) {
		for(AuctionItem item : auctionItems) {
			if(item.slot == slot) return item;
		}
		return null;
	}

	public static File getSaveFile(int slot) {
		return new File(BungeeMain.INSTANCE.getDataFolder() + "/auctions/" + slot + ".json");
	}

	public static void loadAuction(int slot) {
		File file = getSaveFile(slot);
		if(!file.exists()) {
			file.mkdir();
			auctionItems[slot] = new AuctionItem(slot, generateSeed(), generateSeed(), new LinkedHashMap<>());
			return;
		}

		try {
			Reader reader = Files.newBufferedReader(file.toPath());
			auctionItems[slot] = gson.fromJson(reader, AuctionItem.class);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		endTime = generateEndTime();
	}

	public static long generateSeed() {
		return (long) (Math.random() * Long.MAX_VALUE);
	}

	public static void sendAuctionsToServer(String serverName) {
		for(AuctionItem auctionItem : auctionItems) {
			auctionItem.sendDataToServer(serverName);
		}
	}

	public static long generateEndTime() {
		return System.currentTimeMillis() + (new Random().nextInt(60 * 12) + 60 * 6) * 60 * 1000;
	}

	public static File getRewardFile() {
		File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/auctions/rewards.json");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		return file;
	}

	public static void addItemReward(AuctionRewardManager.AuctionItemReward reward) {
		auctionRewardManager.itemRewards.add(reward);
	}

	public static void addSoulReturn(AuctionRewardManager.AuctionSoulReturn soulReturn) {
		auctionRewardManager.soulReturns.add(soulReturn);
	}
}
