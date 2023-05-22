package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class BroadcastCommand extends Command {
	public BroadcastCommand(Plugin bungeeMain) {
		super("broadcast", null, "bc");
	}

	public static boolean isEnabled = false;

	@Override
	public void execute(CommandSender commandSender, String[] args) {
		ProxiedPlayer proxiedPlayer = commandSender instanceof ProxiedPlayer ? (ProxiedPlayer) commandSender : null;

		if(args.length < 1) {
			if(proxiedPlayer != null) {
				AOutput.color(proxiedPlayer, "&c&lERROR!&7 Usage: /broadcast <message>");
			} else {
				System.out.println("Usage: /broadcast <message>");
			}
			return;
		}

		if(!isEnabled && !commandSender.hasPermission("pitsim.broadcast")) {
			if(proxiedPlayer != null) {
				AOutput.color(proxiedPlayer, "&c&lERROR!&7 You do not have permission to do that");
				return;
			}
			return;
		}

		broadcast(String.join(" ", args));
	}

	public static void broadcast(String message) {
		broadcast(message, true);
	}

	public static void broadcast(String message, boolean prefix) {
		String broadcastMessage = (prefix ? "&c&lBROADCAST!&7 " : "") + message;



		for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			if(!prefix && !PitSimServerManager.isInPitSim(player)) continue;
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', broadcastMessage)));
		}
	}
}
