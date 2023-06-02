package dev.wiji.instancemanager.auctions;

import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuctionMessaging implements Listener {

	@EventHandler
	public void onMessageReceived(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> ints = message.getIntegers();

		if(strings.size() > 0 && strings.get(0).equals("AUCTION BID")) {
			String serverName = strings.get(1);

			String uuid = strings.get(2);
			int slot = ints.get(0);
			int bid = ints.get(1);

			AuctionItem item = AuctionManager.getAuctionItem(slot);
			if(item == null) {
				respond(serverName, UUID.fromString(uuid), false);
				return;
			}

			UUID playerUUID = UUID.fromString(uuid);
			respond(serverName, playerUUID, item.placeBid(playerUUID, bid));
		}
	}

	public void respond(String server, UUID player, boolean response) {
		PluginMessage message = new PluginMessage();
		message.writeString("AUCTION BID RESPONSE");
		message.writeBoolean(response);
		message.writeString(player.toString());
		message.addServer(server);
		message.send();
	}

	public static void checkForRewards(ProxiedPlayer player) {
		for(AuctionRewardManager.AuctionItemReward item : AuctionManager.auctionRewardManager.itemRewards) {
			if(item == null || !item.playerUUID.equals(player.getUniqueId())) continue;

			((ProxyRunnable) () -> {
				if(!PitSimServerManager.isInPitSim(player)) return;
				PluginMessage message = new PluginMessage().writeString("AUCTION ITEM REWARD");
				message.writeLong(item.itemSeed);
				message.writeLong(item.dataSeed);
				message.writeString(item.playerUUID.toString());
				message.addServer(player.getServer().getInfo());
				message.send();

				AuctionManager.auctionRewardManager.itemRewards.remove(item);
			}).runAfter(3, TimeUnit.SECONDS);
		}

		for(AuctionRewardManager.AuctionSoulReturn item : AuctionManager.auctionRewardManager.soulReturns) {
			if(item == null || !item.playerUUID.equals(player.getUniqueId())) continue;

			((ProxyRunnable) () -> {
				if(!PitSimServerManager.isInPitSim(player)) return;
				PluginMessage message = new PluginMessage().writeString("AUCTION SOUL REWARD");
				message.writeInt(item.amount);
				message.writeString(item.playerUUID.toString());
				message.addServer(player.getServer().getInfo());
				message.send();

				AuctionManager.auctionRewardManager.soulReturns.remove(item);
			}).runAfter(3, TimeUnit.SECONDS);
		}
	}
}
