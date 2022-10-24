package dev.wiji.instancemanager.Guilds.controllers;

import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
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

		Guild guild = GuildManager.getGuild(player.getUniqueId());
		if(guild == null) return;

		event.setCancelled(true);
		guild.chat(player, event.getMessage());
	}
}
