package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.Misc.Constants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TagCommand extends ACommand {
	public TagCommand(AMultiCommand base, String executor) {
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
			if(!entry.getValue().rank.isAtLeast(Constants.TAG_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.TAG_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /tag <name>");
			return;
		}

		String tag = args.get(0);
		if(tag.length() > 5) {
			AOutput.color(player, "Your guild's tag cannot be longer than 5 characters");
			return;
		}
		Pattern pattern = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
		if(pattern.matcher(tag).find()) {
			AOutput.color(player, "Tags can only contain letters");
			return;
		}

		for(Guild testGuild : GuildManager.guildList) {
			if(testGuild.tag == null || !testGuild.tag.equalsIgnoreCase(tag) || testGuild == guild) continue;
			AOutput.color(player, "A guild with that tag already exists");
			return;
		}

		guild.tag = tag;
		guild.save();
		guild.broadcast("&aGUILD! &7Your guild's tag is now " + guild.getColor() + "#" + guild.tag);
	}
}
