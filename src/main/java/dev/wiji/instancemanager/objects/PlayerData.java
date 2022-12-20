package dev.wiji.instancemanager.objects;

import com.google.cloud.firestore.DocumentSnapshot;

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

				int prestige = Objects.requireNonNull(document.getLong("prestige")).intValue();
				int level = Objects.requireNonNull(document.getLong("level")).intValue();
//				long remainingXP = document.getLong("remainingXP");
				dataMap.put(value, (double) (level + prestige * 1000));
				continue;
			}

//			System.out.println(document.get("stats." + value.fireStore));

			dataMap.put(value, document.getDouble("stats." + value.fireStore));
		}

		PlayerData remove = null;

		for(PlayerData playerData : playerDataList) {
			if(playerData.playerUUID.equals(playerUUID)) {
				remove = playerData;
				break;
			}
		}

		if(remove != null) playerDataList.remove(remove);

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

