package dev.wiji.instancemanager.Guilds.commands.guildcommands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.Guilds.events.GuildWithdrawalEvent;
import dev.wiji.instancemanager.Misc.ACommand;
import dev.wiji.instancemanager.Misc.AMultiCommand;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.Misc.Constants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;

public class WithdrawalCommand extends ACommand {
	public WithdrawalCommand(AMultiCommand base, String executor) {
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
			if(!entry.getValue().rank.isAtLeast(Constants.WITHDRAW_PERMISSION)) {
				AOutput.color(player, "You must be at least " + Constants.WITHDRAW_PERMISSION.displayName + " to do this");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.color(player, "Usage: /withdraw <amount>");
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(0));
			if(amount <= 0) throw new IllegalArgumentException();
		} catch(Exception ignored) {
			AOutput.color(player, "Invalid amount");
			return;
		}

		if(amount > guild.getBalance()) {
			AOutput.color(player, "There is not enough money to do this");
			return;
		}

		GuildWithdrawalEvent event = new GuildWithdrawalEvent(player, guild, amount);
		BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(event);
		if(event.isCancelled()) return;

		//TODO: Deposit money to player
//		ArcticGuilds.VAULT.depositPlayer(player, amount);
		guild.withdraw(amount);

		guild.broadcast("&a&lGUILD! &7" + player.getName() + " has withdrawn &6" + ArcticGuilds.decimalFormat.format(amount) + "g");
	}
}
