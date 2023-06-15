package net.pitsim.bungee.guilds.commands.guildcommands;

import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildMember;
import net.pitsim.bungee.guilds.controllers.objects.GuildMemberInfo;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;

public class LeaveCommand extends ACommand {
	public LeaveCommand(AMultiCommand base, String executor) {
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

		if(guild.ownerUUID.equals(player.getUniqueId())) {
			AOutput.color(player, "You cannot leave your own guild");
			return;
		}

//		if(!PermissionManager.isAdmin(player)) {
//			Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
//			if(entry.getKey().wasModifiedRecently()) {
//				AOutput.error(player, "You have changed guilds too recently. Please wait " + entry.getKey().getModifiedTimeRemaining());
//				return;
//			}
//		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		entry.getKey().leave();
		guild.broadcast("&a&lGUILD! &7" + player.getName() + " has left the guild");
		AOutput.color(player, "&a&lGUILD! &7You have left the guild: " + guild.name);
	}
}
