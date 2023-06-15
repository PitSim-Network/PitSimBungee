package net.pitsim.bungee.guilds.commands.guildcommands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.PermissionManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildMember;
import net.pitsim.bungee.guilds.controllers.objects.GuildMemberInfo;
import net.pitsim.bungee.guilds.enums.GuildRank;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.Constants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DemoteCommand extends ACommand {
	public DemoteCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) {
			AOutput.error(player, "You are not in a guild");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(Constants.DEMOTE_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.DEMOTE_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /promote <player>");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> guildTarget = null;

		UUID uuid = BungeeMain.getUUID(args.get(0), false);

		if(uuid == null) {
			AOutput.color(player, "That player does not exist");
			return;
		}
		for(Map.Entry<GuildMember, GuildMemberInfo> memberEntry : guild.members.entrySet()) {
			if(!memberEntry.getKey().playerUUID.equals(uuid)) continue;
			guildTarget = memberEntry;
			break;
		}
		if(guildTarget == null) {
			AOutput.color(player, "That player is not in your guild");
			return;
		}
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(guildTarget.getValue().rank) || entry.getValue().rank == guildTarget.getValue().rank) {
				AOutput.color(player, "You cannot demote someone of an equal or higher rank");
				return;
			}
		}
		if(PermissionManager.isAdmin(player) && guildTarget.getValue().rank == GuildRank.OWNER) {
			AOutput.color(player, "You cannot demote a owner of a guild");
			return;
		}

		if(guildTarget.getValue().rank == GuildRank.RECRUIT) {
			AOutput.color(player, "That player cannot be demote any lower");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(uuid.equals(player.getUniqueId())) {
				AOutput.color(player, "You cannot demote yourself");
				return;
			}
		}

		guildTarget.getValue().rank = guildTarget.getValue().rank.getRelative(-1);
		guild.save();
		guild.broadcast("&a&lGUILD! &7" + args.get(0) + " has been demoted to " + guildTarget.getValue().rank.displayName);
	}

}
