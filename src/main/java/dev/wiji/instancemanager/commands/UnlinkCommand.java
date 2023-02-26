package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.discord.DiscordUser;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.pitsim.IdentificationManager;
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
		AuthenticationManager.attemptAuthentication(proxiedPlayer);

		UUID targetUUID = proxiedPlayer.getUniqueId();
		if(proxiedPlayer.hasPermission("pitsim.admin") && args.length != 0) {
			try {
				targetUUID = UUID.fromString(args[0]);
			} catch(Exception ignored) {
				AOutput.color(proxiedPlayer, "&c&lERROR!&7 Not a valid player");
			}
		}

		DiscordUser discordUser = DiscordManager.getUser(targetUUID);
		if(discordUser == null) {
			AOutput.error(proxiedPlayer, "&c&lERROR!&7 You do not have a discord account linked");
			return;
		}

		long nextVerifyTime = discordUser.lastLink + 1000 * 60 * 60 * 24 * 7;
		long currentTime = System.currentTimeMillis();
		if(nextVerifyTime > currentTime) {
			String timeLeft = Misc.formatDurationFull(nextVerifyTime - currentTime, false);
			AOutput.color(proxiedPlayer, "&c&lERROR!&7 You cannot do this for another " + timeLeft);
			return;
		}

		discordUser.remove();
		if(proxiedPlayer.getUniqueId().equals(targetUUID)) {
			AOutput.color(proxiedPlayer, "&9&lLINK! Unliked your discord account");
		} else {
			try {
				String username = IdentificationManager.getUsername(IdentificationManager.getConnection(), targetUUID);
				AOutput.color(proxiedPlayer, "&9&lLINK! Unliked discord account for " + username);
				ProxiedPlayer proxiedTarget = BungeeMain.INSTANCE.getProxy().getPlayer(targetUUID);
				if(proxiedTarget != null) AOutput.color(proxiedTarget, "&9&lLINK! Unliked your discord account");
			} catch(Exception ignored) {}
		}
	}
}
