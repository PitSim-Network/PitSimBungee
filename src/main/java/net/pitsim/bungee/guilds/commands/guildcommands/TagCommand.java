package net.pitsim.bungee.guilds.commands.guildcommands;

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
			AOutput.error(player, "You are not in a guild");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(Constants.TAG_PERMISSION)) {
				AOutput.error(player, "You must be at least " + Constants.TAG_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /tag <name>");
			return;
		}

		String tag = args.get(0);
		if(tag.length() > 5) {
			AOutput.error(player, "Your guild's tag cannot be longer than 5 characters");
			return;
		}
		Pattern pattern = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
		if(pattern.matcher(tag).find()) {
			AOutput.error(player, "Tags can only contain letters");
			return;
		}

		for(Guild testGuild : GuildManager.guildList) {
			if(testGuild.tag == null || !testGuild.tag.equalsIgnoreCase(tag) || testGuild == guild) continue;
			AOutput.error(player, "A guild with that tag already exists");
			return;
		}

		guild.tag = tag;
		guild.save();
		guild.broadcast("&a&lGUILD! &7Your guild's tag is now " + guild.getColor() + "#" + guild.tag);
	}
}
