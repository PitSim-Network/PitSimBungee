package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.inventories.ConfirmationGUI;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.ProxyRunnable;
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
			AOutput.color(player, "Usage: /guild create <name>");
			return;
		}

		//TODO: Contact server for player balance
//		if(ArcticGuilds.VAULT.getBalance(player) < GUILD_CREATION_COST) {
//			AOutput.color(player, "&7You need to have &6" + ArcticGuilds.decimalFormat.format(GUILD_CREATION_COST) + "g &7to do that");
//			return;
//		}

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

		for(Guild guild : GuildManager.guildList) {
			if(!guild.name.equalsIgnoreCase(name)) continue;
			AOutput.color(player, "A guild with that name already exists");
			return;
		}

		ProxyRunnable disband = () -> {
//				if(ArcticGuilds.VAULT.getBalance(player) < GUILD_CREATION_COST) {
//					AOutput.error(player, "You no longer have sufficient funds to do this");
//					return;
//				}
//				ArcticGuilds.VAULT.withdrawPlayer(player, GUILD_CREATION_COST);
//				Guild guild = new Guild(player, name);
//				AOutput.send(player, "&a&lGUILD! &7You have created a guild with the name: " + guild.name);
		};

		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will create", "&7the guild " + name, "",
				"&7Doing so costs &6" + ArcticGuilds.decimalFormat.format(GUILD_CREATION_COST) + "g");
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");
		new ConfirmationGUI(player, disband, yesLore, noLore).open();
	}
}
