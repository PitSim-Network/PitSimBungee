package dev.wiji.instancemanager.storage.dupe;

import com.google.gson.Gson;
import de.sumafu.PlayerStatus.PlayerNeverConnectedException;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.discord.Constants;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.md_5.bungee.api.plugin.Listener;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DupeManager implements Listener {
	public static boolean running = false;
	public static List<TrackedItem> dupedItems = new ArrayList<>();
	public static List<TrackedMiscItem> miscItems = new ArrayList<>();

	static {
		miscItems.add(new TrackedMiscItem("Feathers", "feathers",
				"***REMOVED***"));
		miscItems.add(new TrackedMiscItem("Gem Shards", "shards",
				"***REMOVED***"));
		miscItems.add(new TrackedMiscItem("Gems", "gems",
				"***REMOVED***"));

		if(!ConfigManager.isDev()) run();
	}

	public static void run() {
		if(running) throw new RuntimeException();
		running = true;
		dupedItems.clear();
		new Thread(() -> {
			Set<UUID> exemptPlayers;
			try {
				exemptPlayers = BungeeMain.LUCKPERMS.getUserManager().searchAll(NodeMatcher.key("pitsim.admin"))
						.thenApply(Map::keySet).get();
			} catch(Exception exception) {
				throw new RuntimeException(exception);
			}

			try {
				MojangsonParser.parse("{}");
			} catch(MojangsonParseException ignored) {}

			List<TrackedItem> trackedItems = new ArrayList<>();
			System.out.println("Stage 1 initiated");

			File folder = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
			File[] fileArray = folder.listFiles();

			int fileCount = Objects.requireNonNull(fileArray).length;
			for(int i = 0; i < fileCount; i++) {
				if(i % 100 == 0) System.out.println("Stage 1: " + i + "/" + fileCount);
				if(!fileArray[i].isFile()) continue;
				StorageProfile storageProfile = loadPlayer(fileArray[i].getName());
				if(storageProfile == null) continue;
				UUID playerUUID = UUID.fromString(fileArray[i].getName().split("\\.")[0]);

				Map<LimitedItemStack, ItemLocation> playerItemMap = new HashMap<>();
				for(int j = 0; j < storageProfile.getInventoryStrings().length; j++) {
					String itemString = storageProfile.getInventoryStrings()[j];
					if(itemString == null || itemString.isEmpty()) continue;
					LimitedItemStack itemStack = deserialize(itemString);
					if(itemStack.nbtData == null) continue;
					playerItemMap.put(itemStack, new ItemLocation.InventoryLocation(j + 1));
				}
				for(int j = 0; j < storageProfile.getArmor().length; j++) {
					String itemString = storageProfile.getArmor()[j];
					if(itemString == null || itemString.isEmpty()) continue;
					LimitedItemStack itemStack = deserialize(itemString);
					if(itemStack.nbtData == null) continue;
					playerItemMap.put(itemStack, new ItemLocation.ArmorLocation(j));
				}
				for(int j = 0; j < storageProfile.getEnderchest().length; j++) {
					for(int k = 0; k < storageProfile.getEnderchest()[j].length; k++) {
						String itemString = storageProfile.getEnderchest()[j][k];
						if(itemString == null || itemString.isEmpty()) continue;
						LimitedItemStack itemStack = deserialize(itemString);
						if(itemStack.nbtData == null) continue;
						playerItemMap.put(itemStack, new ItemLocation.EnderchestLocation(j + 1, k + 1));
					}
				}

				for(Map.Entry<LimitedItemStack, ItemLocation> entry : playerItemMap.entrySet()) {
					LimitedItemStack itemStack = entry.getKey();

					trackMiscItem(playerUUID, itemStack);

					if(!itemStack.nbtData.hasKey(NBTTag.ITEM_UUID.getRef())) continue;
					if(!itemStack.nbtData.hasKey(NBTTag.ITEM_JEWEL_ENCHANT.getRef()) && itemStack.material != Material.CHAINMAIL_CHESTPLATE &&
							itemStack.material != Material.LEATHER_CHESTPLATE && itemStack.material != Material.STONE_HOE &&
							itemStack.material != Material.GOLD_HOE) continue;
					trackedItems.add(new TrackedItem(playerUUID, itemStack, entry.getValue()));
				}
			}
			checkForDuplicates(trackedItems, exemptPlayers);
			running = false;
		}).start();
	}

	public static void checkForDuplicates(List<TrackedItem> trackedItems, Set<UUID> exemptPlayers) {
		List<UUID> playerUUIDs = new ArrayList<>();

		System.out.println("Stage 2 initiated");

		int count = 0;
		for(TrackedItem trackedItem : trackedItems) {
			if(count++ % 100 == 0) System.out.println("Stage 2: " + count + "/" + trackedItems.size());
			UUID trackedItemUUID = trackedItem.itemUUID;
			for(TrackedItem checkItem : trackedItems) {
				if(checkItem == trackedItem) continue;
				if(!checkItem.itemUUID.equals(trackedItemUUID)) continue;

				if(!dupedItems.contains(trackedItem)) dupedItems.add(trackedItem);
				if(!dupedItems.contains(checkItem)) dupedItems.add(checkItem);

				if(!playerUUIDs.contains(trackedItem.playerUUID)) playerUUIDs.add(trackedItem.playerUUID);
				if(!playerUUIDs.contains(checkItem.playerUUID)) playerUUIDs.add(checkItem.playerUUID);
			}
		}

		System.out.println("Stage 3 initiated");

		for(TrackedItem trackedItem : trackedItems) trackedItem.populate();

		System.out.println("Check completed, posting results");

		TextChannel dupeChannel = DiscordManager.PRIVATE_GUILD.getTextChannelById(Constants.DUPE_CHANNEL);
		assert dupeChannel != null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		if(!dupedItems.isEmpty()) dupeChannel.sendMessage(".\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" +
				(ConfigManager.isDev() ? "" : "@everyone ") + "Logging " + dupedItems.size() + " duped item" + (dupedItems.size() == 1 ? "" : "s") + " from " +
				dateFormat.format(Misc.convertToEST(new Date()))).queue();

		List<MessageEmbed> dupeEmbeds = new ArrayList<>();
		EmbedBuilder embedBuilder = null;
		List<UUID> players = new ArrayList<>();
		int timesFound = 0;
		for(int i = 0; i < dupedItems.size(); i++) {
			TrackedItem dupedItem = dupedItems.get(i);
			if(i == 0) {
				embedBuilder = getNextEmbed(dupedItem);
			} else {
				UUID previousUUID = dupedItems.get(i - 1).itemUUID;
				if(!previousUUID.equals(dupedItem.itemUUID)) {
					embedBuilder.setDescription("Item found " + timesFound + " time" + (timesFound == 1 ? "" : "s") + " " +
							(players.size() == 1 ? "on 1 account" : "across " + players.size() + " accounts"));
					dupeEmbeds.add(embedBuilder.build());

					embedBuilder = getNextEmbed(dupedItem);
					players.clear();
					timesFound = 0;
				}
			}
			timesFound++;
			if(!players.contains(dupedItem.playerUUID)) players.add(dupedItem.playerUUID);

			embedBuilder.addField(ChatColor.stripColor(dupedItem.itemStack.displayName), getPlayerName(dupedItem.playerUUID) + "'s " +
					dupedItem.itemLocation.getUnformattedLocation(), true);
		}
		new Thread(() -> {
			while(!dupeEmbeds.isEmpty()) {
				dupeChannel.sendMessage(dupeEmbeds.remove(0)).queue();
				try {
					Thread.sleep(1_000);
				} catch(InterruptedException exception) {
					throw new RuntimeException(exception);
				}
			}
		}).start();

		for(TrackedMiscItem miscItem : miscItems) {
			for(Map.Entry<UUID, Integer> entry : new ArrayList<>(miscItem.itemMap.entrySet()))
				if(exemptPlayers.contains(entry.getKey())) miscItem.itemMap.remove(entry.getKey());
			int total = miscItem.itemMap.values().stream().mapToInt(i -> i).sum();

			DiscordWebhook miscItemWebhook = new DiscordWebhook(miscItem.webhookURI);
			DiscordWebhook.EmbedObject miscItemEmbed = new DiscordWebhook.EmbedObject()
					.setTitle(miscItem.displayName)
					.setDescription("Total " + miscItem.displayName + ": " + total)
					.setColor(Color.BLACK);
			miscItemWebhook.addEmbed(miscItemEmbed);

			Stream<Map.Entry<UUID, Integer>> sorted = miscItem.itemMap.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
					.limit(40);
			AtomicInteger mapCount = new AtomicInteger(1);
			sorted.forEach(entry -> {
				String playerName = getPlayerName(entry.getKey());
				miscItemEmbed.addField(mapCount.getAndIncrement() + ". " + playerName, entry.getValue() + " " + miscItem.displayName, true);
			});

			try {
				miscItemWebhook.execute();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Results posted/queued");
	}

	public static EmbedBuilder getNextEmbed(TrackedItem dupedItem) {
		return new EmbedBuilder()
				.setTitle(dupedItem.itemUUID.toString() + " - " + getMaterialDisplayName(dupedItem.itemStack.material))
				.setColor(getMaterialColor(dupedItem.itemStack.material));
	}

	public static String getMaterialDisplayName(Material material) {
		switch(material) {
			case GOLD_SWORD:
				return "Gold Sword";
			case BOW:
				return "Bow";
			case LEATHER_LEGGINGS:
				return "Leather Leggings";
			case GOLD_HOE:
				return "Gold Hoe";
			case STONE_HOE:
				return "Stone Hoe";
			case LEATHER_CHESTPLATE:
				return "Leather Chestplate";
			case CHAINMAIL_CHESTPLATE:
				return "Chainmail Chestplate";
		}
		return material.toString();
	}

	public static Color getMaterialColor(Material material) {
		switch(material) {
			case GOLD_SWORD:
				return new Color(0xFFFF55);
			case BOW:
				return new Color(0x55FFFF);
			case LEATHER_LEGGINGS:
				return new Color(0x00AA00);
			case GOLD_HOE:
			case STONE_HOE:
				return new Color(0xFF55FF);
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
				return new Color(0xAA00AA);
		}
		return Color.BLACK;
	}

	public static TrackedMiscItem getTrack(String refName) {
		for(TrackedMiscItem miscItem : miscItems) {
			if(miscItem.refName.equalsIgnoreCase(refName)) return miscItem;
		}
		throw new RuntimeException();
	}

	public static void trackMiscItem(UUID uuid, LimitedItemStack itemStack) {
		if(itemStack.nbtData.hasKey(NBTTag.IS_FEATHER.getRef())) {
			Map<UUID, Integer> trackMap = getTrack("feathers").itemMap;
			trackMap.put(uuid, trackMap.getOrDefault(uuid, 0) + itemStack.amount);
		}
		if(itemStack.nbtData.hasKey(NBTTag.IS_SHARD.getRef())) {
			Map<UUID, Integer> trackMap = getTrack("shards").itemMap;
			trackMap.put(uuid, trackMap.getOrDefault(uuid, 0) + itemStack.amount);
		}
		if(itemStack.nbtData.hasKey(NBTTag.IS_GEM.getRef()) || itemStack.nbtData.hasKey(NBTTag.IS_GEMMED.getRef())) {
			Map<UUID, Integer> trackMap = getTrack("gems").itemMap;
			trackMap.put(uuid, trackMap.getOrDefault(uuid, 0) + itemStack.amount);
		}
	}

	public static StorageProfile loadPlayer(String fileName) {
		try {
			File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + fileName);
			Reader reader = Files.newBufferedReader(file.toPath());
			return new Gson().fromJson(reader, StorageProfile.class);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public static LimitedItemStack deserialize(String p) {
		String[] a = p.split("\t");
		return new LimitedItemStack(a[0], a[1], a[2], a[3], a[6]);
	}

	public static String getPlayerName(UUID uuid) {
		try {
			return BungeeMain.psApi.getNameOfUuid(uuid);
		} catch(SQLException | PlayerNeverConnectedException exception) {
			return "ERROR";
		}
	}

	public static class TrackedItem {
		public UUID playerUUID;
		public UUID itemUUID;
		public LimitedItemStack itemStack;
		public ItemLocation itemLocation;
		public List<UUID> sharedWith = new ArrayList<>();

		public TrackedItem(UUID playerUUID, LimitedItemStack itemStack, ItemLocation itemLocation) {
			this.playerUUID = playerUUID;
			this.itemStack = itemStack;
			try {
				this.itemUUID = UUID.fromString(itemStack.nbtData.getString(NBTTag.ITEM_UUID.getRef()));
			} catch(Exception ignored) {}
			this.itemLocation = itemLocation;
		}

		public void populate() {
			for(TrackedItem trackedItem : dupedItems) {
				if(trackedItem == this || !trackedItem.itemUUID.equals(itemUUID)) continue;
				if(!sharedWith.contains(trackedItem.playerUUID)) sharedWith.add(trackedItem.playerUUID);
			}
		}
	}

	public static class TrackedMiscItem {
		public String displayName;
		public String refName;
		public String webhookURI;

		public Map<UUID, Integer> itemMap = new HashMap<>();

		public TrackedMiscItem(String displayName, String refName, String webhookURI) {
			this.displayName = displayName;
			this.refName = refName;
			this.webhookURI = webhookURI;
		}
	}

	public static class LimitedItemStack {
		public Material material;
		public int amount;
		public short data;
		public String displayName;
		public NBTTagCompound nbtData;

		public LimitedItemStack(String material, String amount, String data, String displayName, String nbtData) {
			this.material = Material.getMaterial(material);
			this.amount = Integer.parseInt(amount);
			this.data = Short.parseShort(data);
			this.displayName = displayName;
			try {
				this.nbtData = MojangsonParser.parse(nbtData);
			} catch(MojangsonParseException ignored) {}
		}
	}
}