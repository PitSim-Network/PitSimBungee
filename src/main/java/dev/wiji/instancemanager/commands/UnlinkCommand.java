package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.discord.DiscordUser;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class UnlinkCommand extends Command {

	public UnlinkCommand() {
		super("unlink");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
		boolean isAdmin = proxiedPlayer.hasPermission("pitsim.admin");

		UUID targetUUID = proxiedPlayer.getUniqueId();
		if(isAdmin && args.length != 0) {
			try {
				targetUUID = UUID.fromString(args[0]);
			} catch(Exception ignored) {
				AOutput.color(proxiedPlayer, "&c&lERROR!&7 Not a valid player");
				return;
			}
		}

		DiscordUser discordUser = DiscordManager.getUser(targetUUID);
		if(discordUser == null) {
			if(proxiedPlayer.getUniqueId().equals(targetUUID)) {
				AOutput.error(proxiedPlayer, "&c&lERROR!&7 You do not have a discord account linked");
			} else {
				AOutput.error(proxiedPlayer, "&c&lERROR!&7 They do not have a discord account linked");
			}
			return;
		}

//		Because people are abusing /unlink
		if(!isAdmin) {
			AOutput.error(proxiedPlayer, "&c&lERROR!&7 Create a support ticket in the discord to unlink your account");
			return;
		}

		long nextVerifyTime = discordUser.lastLink + 1000 * 60 * 60 * 24 * 7;
		long currentTime = System.currentTimeMillis();
		if(!isAdmin && nextVerifyTime > currentTime) {
			String timeLeft = Misc.formatDurationFull(nextVerifyTime - currentTime, false);
			AOutput.color(proxiedPlayer, "&c&lERROR!&7 You cannot do this for another " + timeLeft);
			return;
		}

		discordUser.remove();
		if(proxiedPlayer.getUniqueId().equals(targetUUID)) {
			AOutput.color(proxiedPlayer, "&9&lLINK!&7 Unliked your discord account");
		} else {
			try {
				String username = BungeeMain.getName(targetUUID, false);
				AOutput.color(proxiedPlayer, "&9&lLINK!&7 Unliked discord account for " + username);
				ProxiedPlayer proxiedTarget = BungeeMain.INSTANCE.getProxy().getPlayer(targetUUID);
				if(proxiedTarget != null) AOutput.color(proxiedTarget, "&9&lLINK!&7 Unliked your discord account");
			} catch(Exception ignored) {}
		}
	}
}
