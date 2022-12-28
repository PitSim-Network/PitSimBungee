package dev.wiji.instancemanager.misc;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class APlayer {
	public UUID pUUID;
	public File playerFile;
	public Configuration playerData;

	public APlayer(UUID pUUID, File playerFile) {
		this.pUUID = pUUID;
		this.playerFile = playerFile;
		try {
			this.playerData = ConfigurationProvider.getProvider(YamlConfiguration.class).load(playerFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.playerData, this.playerFile);
		} catch(IOException var2) {
			var2.printStackTrace();
		}

	}
}
