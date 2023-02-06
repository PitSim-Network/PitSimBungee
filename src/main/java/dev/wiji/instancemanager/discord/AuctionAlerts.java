package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.Objects;

public class AuctionAlerts implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();

		if(strings.size() > 0 && strings.get(0).equals("AUCTION ALERT")) {
			alert(strings.get(1));
		}
	}

	public void alert(String message) {
		TextChannel channel = Objects.requireNonNull(DiscordManager.JDA.getTextChannelById(Constants.AUCTION_CHANNEL));
		channel.sendMessage(message);
	}
}
