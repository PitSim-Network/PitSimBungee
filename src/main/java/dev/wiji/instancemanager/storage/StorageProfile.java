package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StorageProfile {
	private transient UUID uuid;
	private transient File saveFile;
	private final String[] inventory = new String[36];
	private final String[] armor = new String[4];
	private final EnderchestPage[] enderchestPages = new EnderchestPage[StorageManager.MAX_ENDERCHEST_PAGES];

	private int defaultOverworldSet = -1;
	private int defaultDarkzoneSet = -1;
	private final Outfit[] outfits = new Outfit[9];

	public StorageProfile() {
		for(int i = 0; i < enderchestPages.length; i++) enderchestPages[i] = new EnderchestPage(i);
		for(int i = 0; i < outfits.length; i++) outfits[i] = new Outfit(i);
	}

	public void init(UUID player) {
		this.uuid = player;
		this.saveFile = StorageManager.getStorageFile(player);
	}

	public UUID getUUID() {
		return uuid;
	}

	public String[] getInventory() {
		return inventory;
	}

	public String[] getArmor() {
		return armor;
	}

	public EnderchestPage[] getEnderchestPages() {
		return enderchestPages;
	}

	public EnderchestPage getEnderchestPage(int index) {
		return enderchestPages[index];
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
		sendToServer(server, false);
	}

	public void sendToServer(ServerInfo server, boolean viewOnly) {
		PluginMessage message = new PluginMessage()
				.writeString("PLAYER DATA")
				.writeString(uuid.toString())
				.writeInt(defaultOverworldSet)
				.writeInt(defaultDarkzoneSet)
				.addServer(server);

		message.writeBoolean(viewOnly);
		for(String itemString : inventory) message.writeString(itemString);
		for(String armorString : armor) message.writeString(armorString);
		for(EnderchestPage enderchestPage : enderchestPages) enderchestPage.writeData(message);
		for(Outfit outfit : outfits) outfit.writeData(message);

		message.send();

		if(!viewOnly) Objects.requireNonNull(PitSimServer.getServer(server)).addProfile(this);
	}

	public void updateData(PluginMessage message, String server, boolean logout) {
		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();

		if(logout) {
			PitSimServer pitSimServer = PitSimServer.getServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
			if(pitSimServer == null) {
				//TODO: Critical Error
				System.out.println("CRITICAL ERROR WITH PLAYER " + uuid);
				return;
			}
			pitSimServer.removeProfile(this);
		}

		defaultOverworldSet = integers.remove(0);
		defaultDarkzoneSet = integers.remove(0);
		for(int i = 0; i < 36; i++) inventory[i] = strings.remove(0);
		for(int i = 0; i < 4; i++) armor[i] = strings.remove(0);
		for(EnderchestPage enderchestPage : enderchestPages) enderchestPage.updateData(message);
		for(Outfit outfit : outfits) outfit.updateData(message);

		save();
		new PluginMessage()
				.writeString("SAVE CONFIRMATION")
				.writeString(uuid.toString())
				.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server))
				.send();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public File getSaveFile() {
		return saveFile;
	}

	public void setSaveFile(File saveFile) {
		this.saveFile = saveFile;
	}

	public int getDefaultOverworldSet() {
		return defaultOverworldSet;
	}

	public int getDefaultDarkzoneSet() {
		return defaultDarkzoneSet;
	}

	public Outfit[] getOutfits() {
		return outfits;
	}
}
