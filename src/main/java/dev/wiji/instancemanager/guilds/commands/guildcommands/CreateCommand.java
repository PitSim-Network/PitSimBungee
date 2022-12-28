package dev.wiji.instancemanager.guilds.commands.guildcommands;

import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.guilds.GuildMessaging;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.guilds.inventories.ConfirmationGUI;
import dev.wiji.instancemanager.misc.ACommand;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import dev.wiji.instancemanager.misc.AMultiCommand;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.regex.Pattern;

public class CreateCommand extends ACommand {
	public static int GUILD_CREATION_COST = 500_000;

	public CreateCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild preGuild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(preGuild != null) {
			AOutput.error(player, "You are already in a guild");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			GuildMember guildMember = GuildManager.getMember(player.getUniqueId());
			if(guildMember.wasModifiedRecently()) {
				AOutput.error(player, "You have changed guilds too recently. Please wait " + guildMember.getModifiedTimeRemaining());
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /guild create <name>");
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

		for(Guild guild : GuildManager.guildList) {
			if(!guild.name.equalsIgnoreCase(name)) continue;
			AOutput.error(player, "A guild with that name already exists");
			return;
		}

		ProxyRunnable create = () -> {

			ProxyRunnable success = () -> {
				Guild guild = new Guild(player, name);
				AOutput.color(player, "&a&lGUILD! &7You have created a guild with the name: " + guild.name);
			};

			ProxyRunnable fail = () -> {
				AOutput.error(player, "You do not have enough funds to do this!");
			};
//
			GuildMessaging.deposit(player, GUILD_CREATION_COST, success, fail);
		};

		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will create", "&7the guild " + name, "",
				"&7Doing so costs &6" + ArcticGuilds.decimalFormat.format(GUILD_CREATION_COST) + "g");
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");
		String invName = ChatColor.GRAY + "Create Confirmation GUI";

		new ConfirmationGUI(player, create, yesLore, noLore, invName).open();
	}
}
