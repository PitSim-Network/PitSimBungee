package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Misc.*;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RenameCommand extends ACommand {
	public static int GUILD_RENAME_COST = 100_000;

	public RenameCommand(AMultiCommand base, String executor) {
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
			if(!entry.getValue().rank.isAtLeast(Constants.RENAME_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.RENAME_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /guild rename <name>");
			return;
		}

		if(guild.getBalance() < GUILD_RENAME_COST) {
			AOutput.color(player, "&7You need to have &6" + ArcticGuilds.decimalFormat.format(GUILD_RENAME_COST) + "g &7to do that");
			return;
		}

		String name = args.get(0);
		if(name.length() > 16) {
			AOutput.color(player, "Your guild's name cannot be longer than 16 characters");
			return;
		}
		Pattern pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
		if(pattern.matcher(name).find()) {
			AOutput.color(player, "Names can only contain numbers and letters");
			return;
		}

		if(guild.name.equals(name)) {
			AOutput.color(player, "Your guild already has that name");
			return;
		}

		for(Guild testGuild : GuildManager.guildList) {
			if(testGuild == guild || !testGuild.name.equalsIgnoreCase(name)) continue;
			AOutput.color(player, "A guild with that name already exists");
			return;
		}

		ProxyRunnable rename = () -> {
			if(guild.getBalance() < GUILD_RENAME_COST) {
				AOutput.color(player, "You no longer have sufficient funds to do this");
				return;
			}
			guild.withdraw(GUILD_RENAME_COST);
			guild.name = name;
			guild.save();
			guild.broadcast("&a&lGUILD! &7Guild name changed to: " + name);
		};
		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will rename", "&7your guild to " + name, "",
				"&7Doing so costs &6" + ArcticGuilds.decimalFormat.format(GUILD_RENAME_COST) + "g");
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");


		//TODO: Open confirmation GUI on frontend
//		new ConfirmationGUI(player, rename, yesLore, noLore).open();
	}
}
