package dev.wiji.instancemanager.Objects;

import com.google.cloud.firestore.DocumentSnapshot;
import dev.wiji.instancemanager.PitSim.PrestigeValues;

import java.util.*;

public class PlayerData {

	public static List<PlayerData> playerDataList = new ArrayList<>();

	private final UUID playerUUID;
	private final DocumentSnapshot document;

	private final Map<Leaderboard, Double> dataMap;


	public PlayerData(UUID uuid, DocumentSnapshot document) {
		this.playerUUID = uuid;
		this.document = document;

		dataMap = new HashMap<>();

		for(Leaderboard value : Leaderboard.values()) {
			if(value == Leaderboard.XP) {

				dataMap.put(value, PrestigeValues.getTotalXP((int) document.get("prestige"), (int) document.get("level"), (long) document.get("remainingXP")));
				continue;
			}

			System.out.println(document.get("stats." + value.fireStore));

			dataMap.put(value, (Double) document.get("stats." + value.fireStore));
		}

		playerDataList.add(this);
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public DocumentSnapshot getDocument() {
		return document;
	}

	public Double getData(Leaderboard leaderboard) {
		return dataMap.get(leaderboard);
	}

	public static PlayerData getPlayerData(UUID uuid) {
		for(PlayerData playerData : playerDataList) {
			if(playerData.playerUUID.equals(uuid)) return playerData;
		}
		return null;
	}

}

