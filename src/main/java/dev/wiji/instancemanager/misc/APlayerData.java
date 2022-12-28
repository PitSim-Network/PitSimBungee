package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class APlayerData {
	public static Map<UUID, File> storedPlayers = new HashMap();

	public APlayerData() {
	}

	public static void init() {
		File folder = new File(BungeeMain.INSTANCE.getDataFolder(), "playerdata/");
		if(folder.exists()) {
			File[] files = folder.listFiles();
			File[] var2 = files;
			int var3 = files.length;

			for(int var4 = 0; var4 < var3; ++var4) {
				File file = var2[var4];
				if(file.isFile() && file.getName().endsWith(".yml")) {
					try {
						UUID pUUID = UUID.fromString(file.getName().replaceFirst("[.][^.]+$", ""));
						storedPlayers.put(pUUID, file);
						new APlayer(pUUID, file);
					} catch(Exception var8) {
					}
				}
			}

		}
	}

	public static APlayer getPlayerData(ProxiedPlayer player) {
		return getPlayerData(player.getUniqueId());
	}

	public static APlayer getPlayerData(UUID uuid) {
		return !storedPlayers.containsKey(uuid) ? createPlayerData(uuid) : new APlayer(uuid, storedPlayers.get(uuid));
	}

	public static Map<UUID, APlayer> getAllData() {
		Map<UUID, APlayer> playerMap = new HashMap();
		Iterator var1 = storedPlayers.entrySet().iterator();

		while(var1.hasNext()) {
			Map.Entry<UUID, File> entry = (Map.Entry) var1.next();
			playerMap.put(entry.getKey(), new APlayer((UUID) entry.getKey(), (File) entry.getValue()));
		}

		return playerMap;
	}

	private static APlayer createPlayerData(UUID uuid) {
		File playerFile = new File(BungeeMain.INSTANCE.getDataFolder() + "/playerdata/", uuid + ".yml");
		if(!playerFile.exists()) {
			try {
				playerFile.getParentFile().mkdirs();
				playerFile.createNewFile();
			} catch(IOException var3) {
				var3.printStackTrace();
			}
		}

		storedPlayers.put(uuid, playerFile);
		return new APlayer(uuid, playerFile);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static void savePlayerData(ProxiedPlayer player) {
		savePlayerData(player.getUniqueId());
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static void savePlayerData(UUID uuid) {
		getPlayerData(uuid).save();
	}
}
