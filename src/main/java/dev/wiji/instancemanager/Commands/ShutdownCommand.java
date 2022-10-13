package dev.wiji.instancemanager.Commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Objects.DarkzoneServer;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.Objects.ServerStatus;
import dev.wiji.instancemanager.PitSim.DarkzoneServerManager;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class ShutdownCommand extends Command {
	public ShutdownCommand(Plugin bungeeMain) {
		super("shutdown");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(!player.hasPermission("pitsim.shutdown")) return;

		if(args.length != 1) {
			player.sendMessage((new ComponentBuilder("Invalid arguments! /shutdown <minutes>").color(ChatColor.RED).create()));
		}

		int minutes = 0;

		try {
			minutes = Integer.parseInt(args[0]);
		} catch(Exception e) {
			player.sendMessage((new ComponentBuilder("Invalid arguments! /shutdown <minutes>").color(ChatColor.RED).create()));
			return;
		}

		if(minutes < 1) {
			player.sendMessage((new ComponentBuilder("Invalid arguments! /shutdown <minutes>").color(ChatColor.RED).create()));
			return;
		}

		Server server = player.getServer();
		for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
			if(server.getInfo() == pitSimServer.getServerInfo()) {
				pitSimServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
				new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(pitSimServer.getServerInfo()).send();
			}
		}

		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(server.getInfo() == darkzoneServer.getServerInfo()) {
				darkzoneServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
				new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(darkzoneServer.getServerInfo()).send();
			}
		}

	}
}
