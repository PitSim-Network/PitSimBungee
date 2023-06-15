package net.pitsim.bungee.discord;

import net.pitsim.bungee.alogging.LogManager;
import net.pitsim.bungee.alogging.LogType;
import net.pitsim.bungee.market.MarketListing;
import net.pitsim.bungee.misc.CustomSerializer;
import net.pitsim.bungee.misc.Misc;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Objects;
import java.util.UUID;

public class MarketLog {

	public static void log(MarketListing listing, LogType type, UUID uuid, int[] param) {
		if(DiscordManager.JDA == null) return;

		String message = getMessage(listing, type, uuid, param);

		assert message != null;
		LogManager.logProxyMessage(type, message.replaceAll("\n", ""));

		message = "```" + message + "```";
		TextChannel channel = Objects.requireNonNull(DiscordManager.JDA.getTextChannelById(Constants.MARKET_CHANNEL));
		channel.sendMessage(message).queue();
	}

	public static String getMessage(MarketListing listing, LogType type, UUID uuid, int[] param) {
		switch(type) {
			case CREATE_LISTING:
				return uuid + " created a listing for " + getDisplayName(listing) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_BIN:
				return uuid + " BIN'd " + getDisplayName(listing) + " " + getBin(listing, param) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_BID:
				return uuid + " bid " + param[0] + " Souls on " + getDisplayName(listing) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_CANCEL:
				return uuid + " cancelled listing " + getDisplayName(listing) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_END:
				return "listing for " + getDisplayName(listing) + " expired " + expired(listing) + "\n"+ getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_STAFF_END:
				return "staff member: " + uuid + " ended listing for " + getDisplayName(listing) + expired(listing) + "\n"+ getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_CLAIM_ITEM:
				return uuid + " claimed " + getDisplayName(listing) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			case MARKET_CLAIM_SOULS:
				return uuid + " claimed " + param[0] + " Souls from " + getDisplayName(listing) + "\n" + expired(listing) + "\n" + getOptions(listing) + "\n" + getEnd(listing);
			default:
				return null;
		}
	}

	public static String getDisplayName(MarketListing listing) {
		CustomSerializer.LimitedItemStack stack = listing.getDeserializedData();
		String displayName = stack.displayName;
		if(listing.getOriginalStock() > 1) displayName += " x" + listing.getOriginalStock();
		return displayName;
	}

	public static String getOptions(MarketListing listing) {
		return " STARTING BID: " + listing.getStartingBid() + " BIN: " + listing.getBinPrice() + " DURATION: " +
				Misc.longToTimeFormatted(listing.getListingLength()) + " TIME LEFT: " +
				Misc.longToTimeFormatted(listing.getTimeLeft());
	}

	public static String getEnd(MarketListing listing) {
		return " LISTING CREATOR: " + listing.getOwnerUUID() + " TIME: " + System.currentTimeMillis() + " ID: " + listing.getUUID();
	}

	public static String getBin(MarketListing listing, int[] param) {
		String message = "for " + param[0] + " Souls";
		if(param.length > 1) message = "(x" + param[1] + ") " + message;
		return message;
	}

	public static String expired(MarketListing listing) {
		String message = "";
		if(listing.getStartingBid() != -1) {
			message += "HIGHEST BID: " + listing.getHighestBidder() + ":" + listing.getHighestBid() + " ";
			message += "BID MAP: " + listing.getBidMap();
		}

		if(listing.getBinPrice() != -1 && !listing.isStackBIN()) {
			message += listing.getStartingBid() != -1 ? " " : "" + "BUYER: " + listing.getBuyer();
		}
		if(listing.isStackBIN()) {
			message += "REMAINING STOCK: " + listing.getDeserializedData().amount + "/" + listing.getOriginalStock();
		}

		return message;
	}
}
