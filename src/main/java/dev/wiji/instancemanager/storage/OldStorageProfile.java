package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class OldStorageProfile {
	public static final int ENDERCHEST_PAGES = 18;

	public transient UUID uuid;
	public transient File saveFile;
	public int enderChestPages = ENDERCHEST_PAGES;
	public final String[] inventoryStrings = new String[36];
	public final String[][] enderchest = new String[enderChestPages][27];
	public final String[] armor = new String[4];

	public OldStorageProfile() {}

	public void init(UUID player) {
		this.uuid = player;
		this.saveFile = StorageManager.getStorageFile(player);
	}

	public UUID getUUID() {
		return uuid;
	}

	public void save() {
		StorageProfile storageProfile = new StorageProfile();
		storageProfile.setUuid(uuid);
		storageProfile.setSaveFile(saveFile);
		for(int i = 0; i < storageProfile.getInventory().length; i++) {
			storageProfile.getInventory()[i] = inventoryStrings[i];
		}
		for(int i = 0; i < storageProfile.getArmor().length; i++) {
			storageProfile.getArmor()[i] = armor[i];
		}
		for(int i = 0; i < 18; i++) {
			for(int j = 0; j < 27; j++) {
				storageProfile.getEnderchestPages()[i].getItemStrings()[j] = enderchest[i][j];
			}
		}

		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(storageProfile);
			FileWriter writer = new FileWriter(saveFile.toPath().toString());
			writer.write(json);
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
