package dev.wiji.instancemanager.Commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Skywars.SkywarsGameManager;
import dev.wiji.instancemanager.Skywars.SkywarsQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class BetaCommad extends Command {
	public BetaCommad(Plugin bungeeMain) {
		super("beta");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("dev"));
	}
}
