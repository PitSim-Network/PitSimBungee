package dev.wiji.instancemanager.guilds.commands.guildcommands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.guilds.GuildMessaging;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.events.GuildDepositEvent;
import dev.wiji.instancemanager.misc.ACommand;
import dev.wiji.instancemanager.misc.AMultiCommand;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class DepositCommand extends ACommand {
	public DepositCommand(AMultiCommand base, String executor) {
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

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /deposit <amount>");
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(0));
			if(amount <= 0) throw new IllegalArgumentException();
		} catch(Exception ignored) {
			AOutput. error(player, "Invalid amount");
			return;
		}

		if(guild.getBalance() + amount > guild.getMaxBank()) {
			AOutput.error(player, "Bank is too full");
			return;
		}

		ProxyRunnable success = () -> {
			Guild depositGuild = GuildManager.getGuildFromPlayer(player.getUniqueId());
			if(depositGuild == null) {
				AOutput.error(player, "You are not in a guild");
				return;
			}
			depositGuild.deposit(amount);

			depositGuild.broadcast("&a&lGUILD! &7" + player.getName() + " has deposited &6" + ArcticGuilds.decimalFormat.format(amount) + "g");
		};

		ProxyRunnable fail = () -> {
			AOutput.error(player,"You do not have enough funds");
		};

		GuildMessaging.deposit(player, amount, success, fail);

		GuildDepositEvent event = new GuildDepositEvent(player, guild, amount);
		BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(event);
	}
}
