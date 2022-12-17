package dev.wiji.instancemanager.objects;

public enum Leaderboard {
	XP("xp", "null"),
	GOLD_GRINDED("total-gold", "totalGold"),
//	PLAYER_KILLS("player-kills", "playerKills"),
	BOT_KILLS("bot-kills", "botKills"),
	PLAYTIME("minutes-played", "minutesPlayed"),
	UBERS_COMPLETED("ubers-completed", "ubersCompleted"),
	JEWELS_COMPLETED("jewels-completed", "jewelsCompleted"),
	FEATHERS_LOST("feathers-lost", "feathersLost"),
	BOSSES_KILLED("bosses-killed", "bossesKilled"),
	LIFETIME_SOULS("lifetime-souls", "lifetimeSouls"),
	AUCTIONS_WON("auctions-won", "auctionsWon"),
	HIGHEST_BID("highest-bid", "highestBid");

	public String refName;
	public String fireStore;

	Leaderboard(String refName, String firestore) {
		this.refName = refName;
		this.fireStore = firestore;
	}
}
