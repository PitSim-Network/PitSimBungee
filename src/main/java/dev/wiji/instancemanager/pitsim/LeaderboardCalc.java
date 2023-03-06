package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.objects.Leaderboard;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PlayerData;
import dev.wiji.instancemanager.objects.PluginMessage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LeaderboardCalc {
	public static Map<Leaderboard, List<PlayerData>> leaderboardPositions = new HashMap<>();

	static {
		((ProxyRunnable) () -> {
			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(!overworldServer.status.isOnline()) continue;
				sendLeaderboardData(overworldServer);
			}
		}).runAfterEvery(60, 15, TimeUnit.SECONDS);
	}

	public static void init() {
		leaderboardPositions.clear();
		for(Leaderboard value : Leaderboard.values()) {
			leaderboardPositions.put(value, new ArrayList<>());
		}

		int total = PlayerData.playerDataList.size();
		int count = 0;
		long startTime = System.currentTimeMillis();
		System.out.println("Recalculating leaderboard with " + total + " player" + (total == 1 ? "" : "s"));

		for(PlayerData playerData : new ArrayList<>(PlayerData.playerDataList)) {
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
			if(count % 100 == 0) System.out.println("Leaderboard Calc Progress: " + count + "/" + total);
			count++;
		}

		long totalSeconds = System.currentTimeMillis() - startTime;
		DecimalFormat decimalFormat = new DecimalFormat("#.###");
		System.out.println("Completed calculations in " + decimalFormat.format(totalSeconds) + "ms");
	}

	public static void sendLeaderboardData(OverworldServer server) {
		PluginMessage message = new PluginMessage();
		message.writeString("LEADERBOARD DATA");
		for(Leaderboard leaderboard : Leaderboard.values()) {
			List<PlayerData> leaderboardPlayers = leaderboardPositions.get(leaderboard);
			List<String> leaderboardStrings = new ArrayList<>();
			for(int i = 0; i < 10; i++) {
				if(i + 1 > leaderboardPlayers.size()) continue;
				PlayerData data = leaderboardPlayers.get(i);
				int prestige = Objects.requireNonNull(data.getDocument().getLong("prestige")).intValue();
				int level = Objects.requireNonNull(data.getDocument().getLong("level")).intValue();

				leaderboardStrings.add(data.getPlayerUUID().toString() + "," + prestige + " " + level + "," +
						BigDecimal.valueOf(data.getData(leaderboard)).toPlainString());
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
		List<PlayerData> leaderboardData = leaderboardPositions.get(leaderboard);
		PlayerData playerData = PlayerData.getPlayerData(uuid);
		if(playerData == null) return leaderboardData.size();
		int position = -1;
		for(int i = 0; i < leaderboardData.size(); i++) {
			PlayerData testData = leaderboardData.get(i);
			if(!testData.getPlayerUUID().equals(playerData.getPlayerUUID())) continue;
			position = i + 1;
			break;
		}

		return position;
	}
}
