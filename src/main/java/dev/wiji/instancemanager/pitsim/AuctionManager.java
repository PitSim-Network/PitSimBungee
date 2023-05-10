package dev.wiji.instancemanager.pitsim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.AuctionItem;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class AuctionManager {

	public static final int AUCTION_NUM = 3;

	public static AuctionItem[] auctionItems = new AuctionItem[AUCTION_NUM];
	public static long endTime = 0;

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

		try {
			Reader reader = Files.newBufferedReader(file.toPath());
			auctionItems[slot] = gson.fromJson(reader, AuctionItem.class);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
