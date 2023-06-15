package net.pitsim.bungee.guilds.controllers.objects;

import net.pitsim.bungee.guilds.enums.GuildRank;
import net.md_5.bungee.config.Configuration;

public class GuildMemberInfo {
	public GuildRank rank;

	public GuildMemberInfo() {
		this(GuildRank.INITIAL_RANK);
	}

	public GuildMemberInfo(GuildRank rank) {
		this.rank = rank;
	}

	public GuildMemberInfo(Configuration memberData) {
		this.rank = GuildRank.getRank(memberData.getString("rank"));
	}

	public void save(Configuration memberData) {
		memberData.set("rank", rank.refName);
	}
}
