package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Guilds.enums.GuildRank;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.Misc.Constants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PromoteCommand extends ACommand {
	public PromoteCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) {
			AOutput.color(player, "You are not in a guild");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(Constants.PROMOTE_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.PROMOTE_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /promote <player>");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> guildTarget = null;
		UUID target = BungeeMain.getUUID(args.get(0), false);
		if(target == null) {
			AOutput.color(player, "That player does not exist");
			return;
		}
		for(Map.Entry<GuildMember, GuildMemberInfo> memberEntry : guild.members.entrySet()) {
			if(!memberEntry.getKey().playerUUID.equals(target)) continue;
			guildTarget = memberEntry;
			break;
		}
		if(guildTarget == null) {
			AOutput.color(player, "That player is not in your guild");
			return;
		}

		if(guildTarget.getValue().rank.isAtLeast(GuildRank.CO_OWNER)) {
			AOutput.color(player, "That player cannot be promoted any higher");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(guildTarget.getValue().rank.getPriority() + 1 >= entry.getValue().rank.getPriority()) {
				AOutput.color(player, "You cannot promote someone of an equal or higher rank");
				return;
			}
		}

		if(!PermissionManager.isAdmin(player)) {
			if(target.equals(player.getUniqueId())) {
				AOutput.color(player, "You cannot promote yourself");
				return;
			}
		}

		guildTarget.getValue().rank = guildTarget.getValue().rank.getRelative(1);
		guild.save();
		guild.broadcast("&a&lGUILD! &7" + args.get(0) + " has been promoted to " + guildTarget.getValue().rank.displayName);
	}

}
