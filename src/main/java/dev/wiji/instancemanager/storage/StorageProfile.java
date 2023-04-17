package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class StorageProfile {
	private transient UUID uuid;
	private transient File saveFile;
	private final String[] inventory = new String[36];
	private final String[] armor = new String[4];
	private final EnderchestPage[] enderchestPages = new EnderchestPage[18];

	public StorageProfile() {}

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

	public void sendToServer(ServerInfo server, boolean wait) {
		PluginMessage message = new PluginMessage()
				.writeString("PLAYER DATA")
				.writeString(uuid.toString())
				.writeInt(inventory.length + armor.length)
				.addServer(server);

		for(String itemString : inventory) message.writeString(itemString);
		for(String armorString : armor) message.writeString(armorString);
		for(EnderchestPage enderchestPage : enderchestPages) enderchestPage.writeData(message);

		message.send();
		Objects.requireNonNull(MainGamemodeServer.getServer(server)).addProfile(this);
	}

	public void updateData(PluginMessage message, String server, boolean logout) {
		if(logout) {
			MainGamemodeServer mainGamemodeServer = MainGamemodeServer.getServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server));
			if(mainGamemodeServer == null) {
				//TODO: Critical Error
				System.out.println("CRITICAL ERROR WITH PLAYER " + uuid);
				return;
			}
			mainGamemodeServer.removeProfile(this);
		}

		for(int i = 0; i < 36; i++) inventory[i] = message.getStrings().remove(0);
		for(int i = 0; i < 4; i++) armor[i] = message.getStrings().remove(0);
		for(EnderchestPage enderchestPage : enderchestPages) enderchestPage.updateData(message);

		save();
		new PluginMessage()
				.writeString("SAVE CONFIRMATION")
				.writeString(uuid.toString())
				.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(server))
				.send();
	}
}
