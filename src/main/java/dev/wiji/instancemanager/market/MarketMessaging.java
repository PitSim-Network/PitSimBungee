package dev.wiji.instancemanager.market;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;

public class MarketMessaging implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> ints = message.getIntegers();
		List<Boolean> booleans = message.getBooleans();
		List<Long> longs = message.getLongs();

		if(strings.size() >= 3 && strings.get(0).equals("CREATE LISTING")) {

			UUID owner = UUID.fromString(strings.get(1));
			String item = strings.get(2);
			int startingBid = ints.get(0);
			int binPrice = ints.get(1);
			boolean isStackBIN = booleans.get(0);
			long duration = longs.get(0);

			MarketListing listing = new MarketListing(owner, item, startingBid, binPrice, isStackBIN, duration);
			MarketManager.listings.add(listing);
			listing.update();
		}

		if(strings.size() >= 2 && strings.get(0).equals("REMOVE LISTING")) {
			UUID owner = UUID.fromString(strings.get(1));
			UUID listingID = UUID.fromString(strings.get(2));

			MarketListing listing = MarketManager.getListing(listingID);

			if(listing == null || listing.getHighestBidder() != null || !owner.equals(listing.getOwnerUUID())) {
				MarketManager.sendFailure(owner, listingID);
				return;
			}

			MarketManager.sendSuccess(owner, listing);
			listing.end();
		}

		if(strings.size() >= 2 && strings.get(0).equals("PLACE MARKET BID")) {
			UUID player = UUID.fromString(strings.get(1));
			UUID listingID = UUID.fromString(strings.get(2));
			int bid = ints.get(0);

			MarketListing listing = MarketManager.getListing(listingID);
			if(listing == null) {
				MarketManager.sendFailure(player, listingID);
				return;
			}

			listing.placeBid(player, bid);
		}

		if(strings.size() >= 3 && strings.get(0).equals("LISTING BIN")) {
			System.out.println("Bin!");
			UUID player = UUID.fromString(strings.get(1));
			UUID listingID = UUID.fromString(strings.get(2));
			int amount = ints.get(0);

			MarketListing listing = MarketManager.getListing(listingID);
			if(listing == null) {
				System.out.println("Listing is null");
				MarketManager.sendFailure(player, listingID);
				return;
			}

			listing.bin(player, amount, false);
		}

		if(strings.size() >= 3 && strings.get(0).equals("CLAIM LISTING ITEM")) {
			System.out.println("Listing Claim!");
			UUID player = UUID.fromString(strings.get(1));
			UUID listingID = UUID.fromString(strings.get(2));

			MarketListing listing = MarketManager.getListing(listingID);
			if(listing == null) {
				System.out.println("Failure 0");
				MarketManager.sendFailure(player, listingID);
				return;
			}

			listing.claimItem(player);
		}

		if(strings.size() >= 3 && strings.get(0).equals("CLAIM LISTING SOULS")) {
			UUID player = UUID.fromString(strings.get(1));
			UUID listingID = UUID.fromString(strings.get(2));

			MarketListing listing = MarketManager.getListing(listingID);
			if(listing == null) {
				MarketManager.sendFailure(player, listingID);
				return;
			}

			listing.claimSouls(player);
		}


	}
}
