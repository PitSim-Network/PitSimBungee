package dev.wiji.instancemanager.guilds.controllers;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ServerManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatManager implements Listener {
	public static List<UUID> guildChatPlayer = new ArrayList<>();

	@EventHandler
	public void onChat(ChatEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if(!guildChatPlayer.contains(player.getUniqueId())) return;
		if(event.getMessage().startsWith("/")) return;

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) return;

		event.setCancelled(true);
		guild.chat(player, event.getMessage());
	}

	@EventHandler
	public void onServerChange(ServerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();

		ServerInfo serverInfo = event.getTarget();
		if(!event.getPlayer().isConnected()) return;
		ServerInfo newServer = BungeeMain.INSTANCE.getProxy().getPlayer(event.getPlayer().getUniqueId()).getServer().getInfo();

		if(ServerManager.isMainGamemodeServer(serverInfo) || !ServerManager.isMainGamemodeServer(newServer)) {
			guildChatPlayer.remove(player.getUniqueId());
		}
	}
}
