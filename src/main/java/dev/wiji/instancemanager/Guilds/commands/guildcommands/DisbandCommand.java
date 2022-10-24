package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.ProxyRunnable;
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
			AOutput.color(player, "You are not in a guild");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(!guild.ownerUUID.equals(player.getUniqueId())) {
				AOutput.color(player, "you are not the owner of your guild");
				return;
			}
		}

		ProxyRunnable disband = new ProxyRunnable() {
			@Override
			public void run() {
				guild.disband();
				AOutput.color(player, "&a&lGUILD! &7You have disbanded the guild " + guild.name);
			}
		};
		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will disband", "&7your guild " + guild.name);
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");

		//TODO: Send confirmation GUI
//		new ConfirmationGUI(player, disband, yesLore, noLore).open();
	}
}
