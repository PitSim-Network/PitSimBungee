package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.Misc.Constants;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InviteCommand extends ACommand {
	public static List<UUID> cooldownList = new ArrayList<>();

	public InviteCommand(AMultiCommand base, String executor) {
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

		Map.Entry<GuildMember, GuildMemberInfo> info = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!info.getValue().rank.isAtLeast(Constants.INVITE_PERMISSION)) {
				AOutput.error(player, "You must be at least " + Constants.INVITE_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /invite <player>");
			return;
		}

		ProxiedPlayer target = null;
		for(ProxiedPlayer onlinePlayer : BungeeMain.INSTANCE.getProxy().getPlayers()) {
			if(!onlinePlayer.getName().equalsIgnoreCase(args.get(0))) continue;
			if(BungeeVanishAPI.isInvisible(onlinePlayer)) continue;
			target = onlinePlayer;
			break;
		}
		if(target == null) {
			AOutput.error(player, "Could not find that player");
			return;
		}

		for(Map.Entry<GuildMember, GuildMemberInfo> entry : guild.members.entrySet()) {
			if(!entry.getKey().playerUUID.equals(target.getUniqueId())) continue;
			AOutput.error(player, "That player is already in your guild");
			return;
		}

		if(guild.activeInvites.contains(target.getUniqueId())) {
			AOutput.error(player, "You have already sent an invite to that player");
			return;
		}

		if(guild.members.size() >= guild.getMaxMembers()) {
			AOutput.error(player, "Your guild at its maximum size");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(cooldownList.contains(target.getUniqueId())) {
				AOutput.error(player, "Please wait before trying to invite this player again");
				return;
			}
			cooldownList.add(target.getUniqueId());
			ProxiedPlayer finalTarget = target;
			((ProxyRunnable) () -> cooldownList.remove(finalTarget.getUniqueId())).runAfter(1, TimeUnit.MINUTES);
		}

		guild.activeInvites.add(target.getUniqueId());

		guild.broadcast("&a&lGUILD! &7" + target.getName() + " has been invited to the guild by " + player.getName());
		AOutput.error(target, "&a&lGUILD! &7You have been invited to join " + guild.name + " by " + player.getName());
	}
}
