package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.CommandSender;
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
