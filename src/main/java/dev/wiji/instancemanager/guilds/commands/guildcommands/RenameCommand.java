package dev.wiji.instancemanager.guilds.commands.guildcommands;

import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.guilds.inventories.ConfirmationGUI;
import dev.wiji.instancemanager.misc.*;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;
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
			AOutput.error(player, "You are not in a guild");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		if(!PermissionManager.isAdmin(player)) {
			if(!entry.getValue().rank.isAtLeast(Constants.RENAME_PERMISSION)) {
				AOutput.error(player, "You must be at least " + Constants.RENAME_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /guild rename <name>");
			return;
		}

		if(guild.getBalance() < GUILD_RENAME_COST) {
			AOutput.color(player, "&7You need to have &6" + ArcticGuilds.decimalFormat.format(GUILD_RENAME_COST) + "g &7to do that");
			return;
		}

		String name = args.get(0);
		if(name.length() > 16) {
			AOutput.error(player, "Your guild's name cannot be longer than 16 characters");
			return;
		}
		Pattern pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
		if(pattern.matcher(name).find()) {
			AOutput.error(player, "Names can only contain numbers and letters");
			return;
		}

		if(guild.name.equals(name)) {
			AOutput.error(player, "Your guild already has that name");
			return;
		}

		for(Guild testGuild : GuildManager.guildList) {
			if(testGuild == guild || !testGuild.name.equalsIgnoreCase(name)) continue;
			AOutput.error(player, "A guild with that name already exists");
			return;
		}

		guild.nameChange = name;

		ProxyRunnable rename = () -> {
			Guild renameGuild = GuildManager.getGuildFromPlayer(player.getUniqueId());
			if(renameGuild == null) {
				AOutput.error(player, "You are not in a guild");
				return;
			}

			if(renameGuild.getBalance() < GUILD_RENAME_COST) {
				AOutput.error(player, "You no longer have sufficient funds to do this");
				return;
			}

			if(renameGuild.nameChange == null) {
				AOutput.error(player, "You no longer have a pending rename");
				return;
			}

			renameGuild.withdraw(GUILD_RENAME_COST);
			renameGuild.name = renameGuild.nameChange;
			renameGuild.save();
			renameGuild.broadcast("&a&lGUILD! &7Guild name changed to: " + renameGuild.nameChange);
		};
		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will rename", "&7your guild to " + name, "",
				"&7Doing so costs &6" + ArcticGuilds.decimalFormat.format(GUILD_RENAME_COST) + "g");
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");

		String invName = ChatColor.GRAY + "Rename Confirmation GUI";
		new ConfirmationGUI(player, rename, yesLore, noLore, invName).open();
	}
}
