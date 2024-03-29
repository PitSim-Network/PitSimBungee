package net.pitsim.bungee.guilds.commands.guildcommands;


import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.PermissionManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildMember;
import net.pitsim.bungee.guilds.controllers.objects.GuildMemberInfo;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.Constants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KickCommand extends ACommand {
	public KickCommand(AMultiCommand base, String executor) {
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
			if(!entry.getValue().rank.isAtLeast(Constants.KICK_PERMISSION)) {
				AOutput.error(player, "You must be at least " + Constants.KICK_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /kick <player>");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> guildTarget = null;
		UUID target;

		try {
			target = UUID.fromString(args.get(0));
		} catch(Exception e) {
			target = BungeeMain.getUUID(args.get(0), false);
		}

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
		if(!PermissionManager.isAdmin(player)) {
			if(guildTarget.getKey().wasModifiedRecently()) {
				AOutput.error(player, "That player has changed guilds too recently. Please wait " + guildTarget.getKey().getModifiedTimeRemaining());
				return;
			}
			if(!entry.getValue().rank.isAtLeast(guildTarget.getValue().rank) || entry.getValue().rank == guildTarget.getValue().rank) {
				AOutput.error(player, "You cannot kick someone of a higher rank");
				return;
			}
		}

		if(target.equals(player.getUniqueId())) {
			AOutput.error(player, "You cannot kick yourself");
			return;
		}

		guildTarget.getKey().leave();
		guild.save();
		String targetName = BungeeMain.getName(target, false);
		guild.broadcast("&a&lGUILD! &7 " + targetName + " has been kicked from the guild");
		ProxiedPlayer targetPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(target);
		if(targetPlayer != null)
			AOutput.color(targetPlayer, "&a&lGUILD! &7You have been kicked the guild: " + guild.name);
	}
}
