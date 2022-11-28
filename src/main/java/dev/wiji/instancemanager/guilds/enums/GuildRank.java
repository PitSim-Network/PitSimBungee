package dev.wiji.instancemanager.guilds.enums;

public enum GuildRank {
	OWNER("Owner", "owner", "***"),
	CO_OWNER("Co-Owner", "coowner", "**"),
	OFFICER("Officer", "officer", "*"),
	MEMBER("Member", "member", "+"),
	RECRUIT("Recruit", "recruit", "-");

	public static GuildRank INITIAL_RANK = RECRUIT;

	public String displayName;
	public String refName;
	public String prefix;

	GuildRank(String displayName, String refName, String prefix) {
		this.displayName = displayName;
		this.refName = refName;
		this.prefix = prefix;
	}

	public int getPriority() {
		for(int i = 0; i < values().length; i++) {
			GuildRank rank = values()[i];
			if(rank != this) continue;
			return values().length - i - 1;
		}
		return -1;
	}

	public static GuildRank getRank(String refName) {
		for(GuildRank value : values()) if(value.refName.equalsIgnoreCase(refName)) return value;
		return null;
	}

	public boolean isAtLeast(GuildRank rank) {
		return getPriority() >= rank.getPriority();
	}

	public GuildRank getRelative(int higherAmount) {
		for(int i = 0; i < values().length; i++) {
			if(values()[i] == this) return values()[i - higherAmount];
		}
		return null;
	}
}
