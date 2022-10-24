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

public class MenuCommand extends ACommand {
	public MenuCommand(AMultiCommand base, String executor) {
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
			if(!entry.getValue().rank.isAtLeast(Constants.UPGRADES_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.UPGRADES_PERMISSION.displayName + " to do this");
				return;
			}
		}

		//TODO: Open Meny GUI in frontend
//		new MenuGUI(player, guild).open();
	}
}
