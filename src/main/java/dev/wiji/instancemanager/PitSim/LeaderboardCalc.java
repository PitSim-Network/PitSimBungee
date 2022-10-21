package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.Objects.Leaderboard;
import dev.wiji.instancemanager.Objects.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardCalc {
	public static Map<Leaderboard, List<PlayerData>> leaderboardPositions = new HashMap<>();

	public static void init() {
		for(Leaderboard value : Leaderboard.values()) {
			leaderboardPositions.put(value, new ArrayList<>());
		}

		for(PlayerData playerData : PlayerData.playerDataList) {
			for(Leaderboard leaderboard : Leaderboard.values()) {
				List<PlayerData> list = leaderboardPositions.get(leaderboard);
				for(int i = 0; i < list.size(); i++) {
					if(playerData.getData(leaderboard) > list.get(i).getData(leaderboard)) {
						list.add(i, playerData);
					}
					list.add(playerData);
				}
			}
		}

		System.out.println(leaderboardPositions);
	}
}
