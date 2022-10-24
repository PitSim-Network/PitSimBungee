package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;

public class JoinCommand extends ACommand {
	public JoinCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild preGuild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(preGuild != null) {
			AOutput.color(player, "You are already in a guild");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			GuildMember guildMember = GuildManager.getMember(player.getUniqueId());
			if(guildMember.wasModifiedRecently()) {
				AOutput.color(player, "You have changed guilds too recently. Please wait " + guildMember.getModifiedTimeRemaining());
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /join <guild>");
			return;
		}

		Guild guild = null;
		for(Guild testGuild : GuildManager.guildList) {
			if(!testGuild.name.equalsIgnoreCase(args.get(0))) continue;
			guild = testGuild;
			break;
		}
		if(guild == null) {
			guild:
			for(Guild testGuild : GuildManager.guildList) {
				for(Map.Entry<GuildMember, GuildMemberInfo> entry : testGuild.members.entrySet()) {
					String playerName = BungeeMain.getName(entry.getKey().playerUUID, false);
					if(playerName == null || !playerName.equalsIgnoreCase(args.get(0))) continue;
					guild = testGuild;
					break guild;
				}
			}
		}
		if(guild == null) {
			AOutput.color(player, "Could not find a guild with that name");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(!guild.activeInvites.contains(player.getUniqueId())) {
				AOutput.color(player, "You do not have an invite to that guild");
				return;
			}
			guild.activeInvites.remove(player.getUniqueId());

			if(guild.members.size() >= guild.getMaxMembers()) {
				AOutput.color(player, "That guild has reached its maximum amount of members");
				return;
			}
		}

		guild.broadcast("&a&lGUILD! &7" + player.getName() + " has joined the guild");
		guild.addMember(player);
		AOutput.color(player, "&a&lGUILD! &7You have joined the guild " + guild.name);
	}
}
