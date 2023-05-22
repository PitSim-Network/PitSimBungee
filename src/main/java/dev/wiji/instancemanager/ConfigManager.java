package dev.wiji.instancemanager;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigManager {
	public static File file;
	public static Configuration configuration;

	public static Map<String, String> miniServerMap = new HashMap<>();

	public static void onEnable() {
		file = new File(BungeeMain.INSTANCE.getDataFolder() + "/config.yml");

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

	public static void save() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void getMiniServerList() {
		for(Object s : configuration.getList("mini-servers")) {
			String[] split = ((String) s).split(":");
			miniServerMap.put(split[0], split[1]);
			ServerManager.inactiveServers.add(split[0]);
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

	public static UUID getControllingGuild() {
		String s = configuration.getString("controlling-guild");
		if(s == null) return null;
		return UUID.fromString(s);
	}

	public static void setControllingGuild(UUID uuid) {
		configuration.set("controlling-guild", uuid.toString());
		save();
	}

	public static boolean isDev() {
		return getProxyServer().equals("3989365c");
	}
}
