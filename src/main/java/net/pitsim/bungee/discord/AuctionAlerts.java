package net.pitsim.bungee.discord;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Objects;

public class AuctionAlerts {

	public static void alert(String message) {
		TextChannel channel = Objects.requireNonNull(DiscordManager.JDA.getTextChannelById(Constants.AUCTION_CHANNEL));
		channel.sendMessage(message).queue();
	}
}
