package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerType;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ServerJoinCommand extends Command {
	public ServerJoinCommand(Plugin bungeeMain) {
		super("join");
	}


	public static List<UUID> permissionBypass = new ArrayList<>();

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(!player.hasPermission("pitsim.join") && !ConfigManager.isDev()) return;

		if(args.length < 1) {
			sendServerList(player);
			return;
		}

		String requestedServerString = args[0];
		ServerInfo requestedServer = BungeeMain.INSTANCE.getProxy().getServerInfo(requestedServerString);

		if(requestedServer == null) {
			AOutput.error(player, "That server does not exist! Run /join for a list of servers");
			return;
		}

		ProxiedPlayer affectedPlayer = player;

		if(args.length >= 2 && player.hasPermission("pitsim.join")) {
			String playerString = args[1];
			ProxiedPlayer targetPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(targetPlayer == null) {
				AOutput.error(player, "That player is not online!");
				return;
			}
			affectedPlayer = targetPlayer;
			if(!permissionBypass.contains(targetPlayer.getUniqueId())) permissionBypass.add(targetPlayer.getUniqueId());
		}

		ServerInfo previousServer = affectedPlayer.getServer().getInfo();

		if(requestedServer == affectedPlayer.getServer().getInfo()) {
			if(player == affectedPlayer) AOutput.error(player, "&cYou are already connected to this server!");
			else AOutput.error(player, "&c" + affectedPlayer.getName() + " is already connected to that server!");
			return;
		}

		for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
			ServerType type = pitSimServer.serverType;

			if(pitSimServer.getServerInfo() == requestedServer) {
				if(previousServer.getName().contains("darkzone") || previousServer.getName().contains("pitsim")) {
					new PluginMessage().writeString("REQUEST " + (type == ServerType.DARKZONE ? "DARKZONE " : "") + "SWITCH").writeString(affectedPlayer.getUniqueId().toString())
							.writeInt(pitSimServer.getServerIndex()).addServer(previousServer).send();
					return;
				}

				PitSimServerManager manager = PitSimServerManager.getManager(type);
				assert manager != null;
				manager.queueFallback(affectedPlayer, pitSimServer.getServerIndex(), previousServer.getName().contains("darkzone"));
				return;
			}
		}


		if(player == affectedPlayer) AOutput.color(player, "&2&lSERVERS &aSent you to " + requestedServer.getName());
		else AOutput.color(player, "&2&lSERVERS &aSent " + affectedPlayer.getName() + " to " + requestedServer.getName());
		if(requestedServer.getName().contains("pitsim") || requestedServer.getName().contains("darkzone")) {
			if(player == affectedPlayer) AOutput.error(player, "&cYou are unable to connect to this server at this time.");
			else AOutput.error(player, affectedPlayer.getName() + " &cis unable to connect to that server at this time.");
			return;
		}
		affectedPlayer.connect(requestedServer);
	}

	public void sendServerList(ProxiedPlayer player) {
		List<ServerInfo> servers = new ArrayList<>(BungeeMain.INSTANCE.getProxy().getServers().values());
		servers.sort(Comparator.comparing(ServerInfo::getName));

		AOutput.color(player, "&6&m--------------------&6<&e&lJOIN&6>&m--------------------");
		for(ServerInfo server : servers) {
			int players = server.getPlayers().size();
			TextComponent textComponent = new TextComponent(Misc.colorize("&6 * &e" + server.getName() +
					" &7- &6" + players + " &7player" + (players == 1 ? "" : "s")));
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + server.getName()));
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Misc.colorize("&7Click to connect!")));
			textComponent.setColor(ChatColor.GOLD);
			if(server == player.getServer().getInfo()) textComponent.addExtra(
					ChatColor.translateAlternateColorCodes('&', " &7(&6current&7)"));
			player.sendMessage(textComponent);
		}
		AOutput.color(player, "&6&m--------------------&6<&e&lJOIN&6>&m--------------------");
	}
}
