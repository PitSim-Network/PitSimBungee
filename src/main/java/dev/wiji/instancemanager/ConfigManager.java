package dev.wiji.instancemanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
	public static File file;
	public static Configuration configuration;

	public static void onEnable() {
		file = new File(ProxyServer.getInstance().getPluginsFolder() + "/config.yml");

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

	public static void getMiniServerList() {
		for(Object s : configuration.getList("mini-servers")) {
			ServerManager.inactiveServers.add((String) s);
		}

	}

	public static void getPitSimServerList() {
		for(Object s : configuration.getList("pitsim-servers")) {
			String[] split = ((String) s).split(":");
			ServerManager.pitSimServers.put(split[0], split[1]);
		}
	}

	public static void getDarkzoneServerList() {
		for(Object s : configuration.getList("darkzone-servers")) {
			String[] split = ((String) s).split(":");
			ServerManager.darkzoneServers.put(split[0], split[1]);
		}
	}

	public static String getProxyServer() {
		return configuration.getString("proxy");
	}

	public static String getLobbyServer() {
		return configuration.getString("lobby");
	}

	public static boolean isDev() {
		return getProxyServer().equals("0b42b386");
	}
}
