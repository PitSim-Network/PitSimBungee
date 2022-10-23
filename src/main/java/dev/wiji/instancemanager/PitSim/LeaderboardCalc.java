package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.Objects.Leaderboard;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PlayerData;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.ProxyRunnable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LeaderboardCalc {

	static {
		((ProxyRunnable) () -> {
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(!pitSimServer.status.isOnline()) continue;
				sendLeaderboardData(pitSimServer);
			}
		}).runAfterEvery(15, 15, TimeUnit.SECONDS);
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

		System.out.println(leaderboardPositions);
	}

	public static void sendLeaderboardData(PitSimServer server) {
		PluginMessage message = new PluginMessage();
		message.writeString("LEADERBOARD DATA");
		for(Leaderboard value : Leaderboard.values()) {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < 10; i++) {
				PlayerData data = leaderboardPositions.get(value).get(i);
				int prestige = Objects.requireNonNull(data.getDocument().getLong("prestige")).intValue();
				int level = Objects.requireNonNull(data.getDocument().getLong("level")).intValue();

				builder.append(data.getPlayerUUID().toString()).append(",").append(prestige).append(" ").append("level")
						.append(",").append(BigDecimal.valueOf(data.getData(value)).toPlainString());
				if(i != 9) builder.append("|");
			}
			message.writeString(builder.toString());
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
		for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
			if(!pitSimServer.status.isOnline()) continue;
			message.addServer(pitSimServer.getServerInfo());
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
