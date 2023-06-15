package net.pitsim.bungee.guilds.commands.guildcommands;

import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.PermissionManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.inventories.ConfirmationGUI;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class DisbandCommand extends ACommand {
	public DisbandCommand(AMultiCommand base, String executor) {
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

		if(!PermissionManager.isAdmin(player)) {
			if(!guild.ownerUUID.equals(player.getUniqueId())) {
				AOutput.error(player, "You are not the owner of your guild");
				return;
			}
		}

		ProxyRunnable disband = new ProxyRunnable() {
			@Override
			public void run() {
				Guild disbandGuild = GuildManager.getGuildFromPlayer(player.getUniqueId());
				if(disbandGuild == null) {
					AOutput.error(player, "You are not in a guild");
					return;
				}
				disbandGuild.disband();
				AOutput.color(player, "&a&lGUILD! &7You have disbanded the guild " + disbandGuild.name);
			}
		};
		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will disband", "&7your guild " + guild.name);
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");

		String name = ChatColor.GRAY + "Disband Confirmation GUI";

		new ConfirmationGUI(player, disband, yesLore, noLore, name).open();
	}
}
