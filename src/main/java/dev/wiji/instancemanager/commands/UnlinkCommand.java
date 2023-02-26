package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.discord.DiscordUser;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnlinkCommand extends Command {

	public UnlinkCommand() {
		super("unlink");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
		AuthenticationManager.attemptAuthentication(proxiedPlayer);

		DiscordUser discordUser = DiscordManager.getUser(proxiedPlayer.getUniqueId());
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
	}
}
