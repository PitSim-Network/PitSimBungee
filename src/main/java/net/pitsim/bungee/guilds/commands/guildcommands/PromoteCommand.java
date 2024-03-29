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
			AOutput.error(player, "You are not in a guild");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(Constants.PROMOTE_PERMISSION)) {
				AOutput.error(player, "You must be at least " + Constants.PROMOTE_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /promote <player>");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> guildTarget = null;
		UUID target = BungeeMain.getUUID(args.get(0), false);
		if(target == null) {
			AOutput.error(player, "That player does not exist");
			return;
		}
		for(Map.Entry<GuildMember, GuildMemberInfo> memberEntry : guild.members.entrySet()) {
			if(!memberEntry.getKey().playerUUID.equals(target)) continue;
			guildTarget = memberEntry;
			break;
		}
		if(guildTarget == null) {
			AOutput.error(player, "That player is not in your guild");
			return;
		}

		if(guildTarget.getValue().rank.isAtLeast(GuildRank.CO_OWNER)) {
			AOutput.error(player, "That player cannot be promoted any higher");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(guildTarget.getValue().rank.getPriority() + 1 >= entry.getValue().rank.getPriority()) {
				AOutput.error(player, "You cannot promote someone of an equal or higher rank");
				return;
			}
		}

		if(!PermissionManager.isAdmin(player)) {
			if(target.equals(player.getUniqueId())) {
				AOutput.error(player, "You cannot promote yourself");
				return;
			}
		}

		guildTarget.getValue().rank = guildTarget.getValue().rank.getRelative(1);
		guild.save();
		guild.broadcast("&a&lGUILD! &7" + args.get(0) + " has been promoted to " + guildTarget.getValue().rank.displayName);
	}

}
