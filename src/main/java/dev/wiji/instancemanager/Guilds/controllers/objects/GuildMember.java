package dev.wiji.instancemanager.Guilds.controllers.objects;

import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Misc.APlayer;
import dev.wiji.instancemanager.Misc.APlayerData;
import net.md_5.bungee.config.Configuration;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.UUID;

public class GuildMember {
	public UUID playerUUID;

//	Savable data
	private UUID guildUUID;
	public Date lastModify = new Date(0);

	public GuildMember(UUID playerUUID) {
		this.playerUUID = playerUUID;

		save();

		GuildManager.guildMemberList.add(this);
	}

	public GuildMember(UUID playerUUID, Configuration playerData) {
		this.playerUUID = playerUUID;

		if(playerData.get("guild") != null) this.guildUUID = UUID.fromString(playerData.getString("guild"));
		if(playerData.get("modify-date") != null) this.lastModify = new Date(playerData.getLong("modify-date"));

		GuildManager.guildMemberList.add(this);
	}

	public void leave() {
		if(guildUUID != null) {
			Guild guild = GuildManager.getGuild(guildUUID);
			guild.members.remove(this);
			guild.save();
		}
		setGuildUUID(null);
		save();
	}

//	This writes to the player's data, not the guild's
	public void save() {
		APlayer aPlayer = APlayerData.getPlayerData(playerUUID);
		Configuration playerData = aPlayer.playerData;

		if(guildUUID == null) playerData.set("guild", null); else playerData.set("guild", guildUUID.toString());
		playerData.set("modify-date", lastModify.getTime());

		aPlayer.save();
	}

	public UUID getGuildUUID() {
		return guildUUID;
	}

	public void setGuildUUID(UUID guildUUID) {
		if(this.guildUUID == guildUUID) return;
		this.guildUUID = guildUUID;
		this.lastModify = new Date();
	}

	public static long MODIFY_TIME = 1000 * 60 * 60 * 1 * 1;
	public boolean wasModifiedRecently() {
		return false;
//		return new Date().getTime() - lastModify.getTime() < MODIFY_TIME;
	}
	public String getModifiedTimeRemaining() {
		DecimalFormat decimalFormat = new DecimalFormat("0.0");
		double timeLeft = (MODIFY_TIME - (new Date().getTime() - lastModify.getTime())) / 1000.0 / 60;
		if(timeLeft <= 60) return decimalFormat.format(timeLeft) + " minutes";
		if(timeLeft <= 60 * 24) return decimalFormat.format(timeLeft / 60.0) + " hours";
		return decimalFormat.format(timeLeft / 60.0 / 24) + " days";
	}
}
