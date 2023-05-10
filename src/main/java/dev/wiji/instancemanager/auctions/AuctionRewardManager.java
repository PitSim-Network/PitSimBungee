package dev.wiji.instancemanager.auctions;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionRewardManager {
	public List<AuctionItemReward> itemRewards = new ArrayList<>();
	public List<AuctionSoulReturn> soulReturns = new ArrayList<>();

	public AuctionRewardManager() {

	}

	public static class AuctionItemReward {
		public UUID playerUUID;
		public long itemSeed;
		public long dataSeed;

		public AuctionItemReward(UUID playerUUID, long itemSeed, long dataSeed) {
			this.playerUUID = playerUUID;
			this.itemSeed = itemSeed;
			this.dataSeed = dataSeed;

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
			if(player != null && PitSimServerManager.isInPitSim(player)) {
				PluginMessage message = new PluginMessage().writeString("AUCTION ITEM REWARD");
				message.writeLong(itemSeed);
				message.writeLong(dataSeed);
				message.writeString(playerUUID.toString());
				message.addServer(player.getServer().getInfo());
				message.send();
			} else AuctionManager.addItemReward(this);
		}
	}

	public static class AuctionSoulReturn {
		public UUID playerUUID;
		public int amount;

		public AuctionSoulReturn(UUID playerUUID, int amount) {
			this.playerUUID = playerUUID;
			this.amount = amount;

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerUUID);
			if(player != null && PitSimServerManager.isInPitSim(player)) {
				PluginMessage message = new PluginMessage().writeString("AUCTION SOUL RETURN");
				message.writeInt(amount);
				message.writeString(playerUUID.toString());
				message.addServer(player.getServer().getInfo());
				message.send();
			} else AuctionManager.addSoulReturn(this);
		}
	}

	public void saveRewards() {
		File file = AuctionManager.getRewardFile();
		String json = AuctionManager.gson.toJson(this);

		try {
			FileWriter writer = new FileWriter(file.toPath().toString());
			writer.write(json);
			writer.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
