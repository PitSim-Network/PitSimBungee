package dev.wiji.instancemanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ConfigManager {
	public static File file;
	public static Configuration configuration;

	public static void onEnable() {
		file = new File(ProxyServer.getInstance().getPluginsFolder() + "/conifg.yml");

		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void onDisable() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void getServerList() {
		for(Object s : configuration.getList("servers")) {
			ServerManager.inactiveServers.add((String) s);
		}

	}

}
