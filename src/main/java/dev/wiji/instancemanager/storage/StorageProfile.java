package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StorageProfile {

	public static final int ENDERCHEST_PAGES = 18;

	private transient UUID uuid;
	private transient File saveFile;
	private int enderChestPages = ENDERCHEST_PAGES;
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
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(this);
			FileWriter writer = new FileWriter(saveFile.toPath().toString());
			writer.write(json);
			writer.close();

		} catch(IOException e) {
			e.printStackTrace();

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
			if(player == null) return;

			player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "An error occurred while saving your data. Please contact a staff member."));
		}

	}

	public void	sendEnderchestToServer(ServerInfo server) {

		PluginMessage message = new PluginMessage().addServer(server);



		message.writeString("ENDERCHEST").writeString(uuid.toString()).writeInt(enderChestPages);

		System.out.println("Test number 2");

		for(String[] itemStrings : enderchest) {
			for(String itemString : itemStrings) {
				message.writeString(itemString);
			}
		}

		message.send();
	}

		public void sendInventoryToServer(ServerInfo server) {

			PluginMessage message = new PluginMessage().addServer(server);

			message.writeString("INVENTORY").writeString(uuid.toString());

			System.out.println("TestItem1:" + inventoryStrings[0]);

			for(String itemString : inventoryStrings) {
				message.writeString(itemString);
			}

			for(String armorString : armor) {
				message.writeString(armorString);
			}

			message.send();
	}



	public void updateEnderchest(PluginMessage message, String server) {

		int totalIndex = 0;

		for(int i = 0; i < enderChestPages; i++) {
			for(int j = 0; j < 27; j++) {
				enderchest[i][j] = message.getStrings().get(totalIndex);
				totalIndex++;
			}
		}

		System.out.println("Test number 1");

		PluginMessage response = new PluginMessage().writeString("ENDERCHEST SAVE").writeString(uuid.toString());
		response.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
		response.send();

		save();
	}

	public void updateInventory(PluginMessage message, String server) {
		System.out.println("size: " + message.getStrings().size());
		for(int i = 0; i < 36; i++) {
			inventoryStrings[i] = message.getStrings().get(i);
		}

		for(int i = 0; i < 4; i++) {
			armor[i] = message.getStrings().get(i + 36);
		}

		System.out.println("TestItem2:" + inventoryStrings[0]);
		PluginMessage response = new PluginMessage().writeString("INVENTORY SAVE").writeString(uuid.toString());
		response.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
		response.send();

		save();
	}
}
