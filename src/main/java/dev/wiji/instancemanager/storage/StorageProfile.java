package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class StorageProfile {

	private transient UUID uuid;
	private transient File saveFile;
	private int enderChestPages = 1;
	private final String[] inventoryStrings = new String[36];
	private final String[][] enderchest = new String[enderChestPages][27];
	private final String[] armor = new String[4];


	public StorageProfile() { }

	public void init(UUID player) {
		this.uuid = player;
		this.saveFile = StorageManager.getStorageFile(player);
	}

	public UUID getUuid() {
		return uuid;
	}

	public File getSaveFile() {
		return saveFile;
	}

	public String[] getInventoryStrings() {
		return inventoryStrings;
	}

	public String[] getEnderchestPage(int index) {
		return enderchest[index];
	}

	public String getItem(int pageIndex, int slotIndex) {
		return enderchest[pageIndex][slotIndex];
	}

	public String getArmorItem(int index) {
		return armor[index];
	}

	public void save() {
		try {
			StorageManager.gson.toJson(this, new FileWriter(saveFile.toPath().toString()));
		} catch(IOException e) {
			e.printStackTrace();

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
			if(player == null) return;

			player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "An error occurred while saving your data. Please contact a staff member."));
		}

	}

	public void	sendEnderchestToServer(PitSimServer server) {

		System.out.println(Arrays.deepToString(enderchest));

		if(enderchest[0][0] == null) {
			return;
		}

		System.out.println("ec 2");

		PluginMessage message = new PluginMessage().addServer(server.getServerInfo());

		System.out.println(enderChestPages);
		System.out.println(inventoryStrings.length);
		System.out.println(Arrays.deepToString(enderchest));

		message.writeString("ENDERCHEST").writeString(uuid.toString()).writeInt(enderChestPages);

		for(String[] itemStrings : enderchest) {
			for(String itemString : itemStrings) {
				message.writeString(itemString);
			}
		}

		message.send();
	}

		public void sendInventoryToServer(PitSimServer server) {
		if(inventoryStrings[0] == null) {
			return;
		}

		PluginMessage message = new PluginMessage().addServer(server.getServerInfo());

		message.writeString("INVENTORY").writeString(uuid.toString());

		for(String itemString : inventoryStrings) {
			message.writeString(itemString);
		}

		message.send();
	}



	public void updateEnderchest(PluginMessage message, String server) {
		System.out.println("echest update");
		System.out.println("Size: " + message.getStrings().size());

		int totalIndex = 0;

		for(int i = 0; i < enderChestPages; i++) {
			for(int j = 0; j < 27; j++) {
				enderchest[i][j] = message.getStrings().get(totalIndex);
				totalIndex++;
			}
		}

		PluginMessage response = new PluginMessage().writeString("ENDERCHEST SAVE").writeString(uuid.toString());
		response.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
		response.send();

		System.out.println("saving");
		save();
	}

	public void updateInventory(PluginMessage message, String server) {
		for(int i = 0; i < 36; i++) {
			inventoryStrings[i] = message.getStrings().get(i);
		}

		for(int i = 0; i < 4; i++) {
			armor[i] = message.getStrings().get(i + 36);
		}

		PluginMessage response = new PluginMessage().writeString("INVENTORY SAVE").writeString(uuid.toString());
		response.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
		response.send();

		save();
	}

}
