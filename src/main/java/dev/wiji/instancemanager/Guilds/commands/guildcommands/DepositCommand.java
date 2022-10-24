package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.events.GuildDepositEvent;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
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
			AOutput.color(player, "You are not in a guild");
			return;
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /deposit <amount>");
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(0));
			if(amount <= 0) throw new IllegalArgumentException();
		} catch(Exception ignored) {
			AOutput. color(player, "Invalid amount");
			return;
		}

		//TODO: Check if the amount is more than the player's balance
//		if(amount > ArcticGuilds.VAULT.getBalance(player)) {
//			AOutput.color(player, "You do not have enough money to do this");
//			return;
//		}

		if(guild.getBalance() + amount > guild.getMaxBank()) {
			AOutput.color(player, "Bank is too full");
			return;
		}

		//TODO: Withdraw money from the player
//		ArcticGuilds.VAULT.withdrawPlayer(player, amount);
		guild.deposit(amount);

		guild.broadcast("&a&lGUILD! &7" + player.getName() + " has deposited &6" + ArcticGuilds.decimalFormat.format(amount) + "g");

		GuildDepositEvent event = new GuildDepositEvent(player, guild, amount);
		BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(event);
	}
}
