package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class StorageProfile {
	public static final int ENDERCHEST_PAGES = 18;

	private transient UUID uuid;
	private transient File saveFile;
	private int enderChestPages = ENDERCHEST_PAGES;
	private final String[]  inventoryStrings = new String[36];
	private final String[][] enderchest = new String[enderChestPages][27];
	private final String[] armor = new String[4];

	public StorageProfile() { }

	public void init(UUID player) {
		this.uuid = player;
		this.saveFile = StorageManager.getStorageFile(player);
	}

	public UUID getUUID() {
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

	public String[] getArmor() {
		return armor;
	}

	public String[][] getEnderchest() {
		return enderchest;
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

	public void sendToServer(ServerInfo server) {
		PluginMessage message = new PluginMessage().addServer(server);

		message.writeString("PLAYER DATA").writeString(uuid.toString());
		message.writeInt(inventoryStrings.length + armor.length);

		for(String itemString : inventoryStrings) {
			message.writeString(itemString);
		}

		for(String armorString : armor) {
			message.writeString(armorString);
		}

		int count = 0;

		for(String[] itemStrings : enderchest) {
			for(String itemString : itemStrings) {
				message.writeString(itemString);
				count++;
			}
		}

		message.writeInt(count);

		message.send();
	}

	public void updateData(PluginMessage message, String server) {

		int totalIndex = 0;

		for(int i = 0; i < enderChestPages; i++) {
			for(int j = 0; j < 27; j++) {
				enderchest[i][j] = message.getStrings().get(totalIndex);
				totalIndex++;
			}
		}

		for(int i = 0; i < 36; i++) {
			inventoryStrings[i] = message.getStrings().get(i + totalIndex);
		}

		for(int i = 0; i < 4; i++) {
			armor[i] = message.getStrings().get((i + totalIndex) + 36);
		}

		PluginMessage response = new PluginMessage().writeString("SAVE CONFIRMATION").writeString(uuid.toString());
		response.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
		response.send();

		save();
	}
}
