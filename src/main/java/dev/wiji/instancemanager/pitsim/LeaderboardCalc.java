package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.Leaderboard;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PlayerData;
import dev.wiji.instancemanager.objects.PluginMessage;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LeaderboardCalc {

	static {
		((ProxyRunnable) () -> {
			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(!overworldServer.status.isOnline()) continue;
				sendLeaderboardData(overworldServer);
			}
		}).runAfterEvery(60, 15, TimeUnit.SECONDS);
	}

	public static Map<Leaderboard, List<PlayerData>> leaderboardPositions = new HashMap<>();

	public static void init() {
		for(Leaderboard value : Leaderboard.values()) {
			leaderboardPositions.put(value, new ArrayList<>());
		}

		for(PlayerData playerData : PlayerData.playerDataList) {
			leaderboardPositions:
			for(Leaderboard leaderboard : Leaderboard.values()) {
				List<PlayerData> list = leaderboardPositions.get(leaderboard);
				for(int i = 0; i < list.size(); i++) {
					if(playerData.getData(leaderboard) > list.get(i).getData(leaderboard)) {
						list.add(i, playerData);
						continue leaderboardPositions;
					}
				}
				list.add(playerData);
			}
		}
	}

	public static void sendLeaderboardData(OverworldServer server) {
		PluginMessage message = new PluginMessage();
		message.writeString("LEADERBOARD DATA");
		for(Leaderboard value : Leaderboard.values()) {
			List<String> leaderboardStrings = new ArrayList<>();
			for(int i = 0; i < 10; i++) {
				if(i + 1 > leaderboardPositions.get(value).size()) continue;
				PlayerData data = leaderboardPositions.get(value).get(i);
				int prestige = Objects.requireNonNull(data.getDocument().getLong("prestige")).intValue();
				int level = Objects.requireNonNull(data.getDocument().getLong("level")).intValue();

				leaderboardStrings.add(data.getPlayerUUID().toString() + "," + prestige + " " + level + "," +
						BigDecimal.valueOf(data.getData(value)).toPlainString());
			}
			message.writeString(String.join("|", leaderboardStrings));
		}
		message.addServer(server.getServerInfo());
		message.send();
	}

	public static void sendLeaderboardPlayerData(UUID uuid) {
		PluginMessage message = new PluginMessage().writeString("LEADERBOARD PLAYER DATA");
		message.writeString(uuid.toString());
		for(Leaderboard value : Leaderboard.values()) {
			message.writeInt(getPosition(uuid, value));
		}
		for(OverworldServer overworldServer : OverworldServerManager.serverList) {
			if(!overworldServer.status.isOnline()) continue;
			message.addServer(overworldServer.getServerInfo());
		}
		message.send();
	}

	public static int getPosition(UUID uuid, Leaderboard leaderboard) {
		List<PlayerData> data = leaderboardPositions.get(leaderboard);
		PlayerData playerData = PlayerData.getPlayerData(uuid);
		if(!data.contains(playerData)) return -1;

		return data.indexOf(playerData) + 1;
	}
}
