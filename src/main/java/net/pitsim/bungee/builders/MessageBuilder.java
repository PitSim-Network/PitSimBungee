package net.pitsim.bungee.builders;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageBuilder {
	public List<String> messages = new ArrayList<>();

	public MessageBuilder(String... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}

	public void addMessages(String... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}

	public void send(ProxiedPlayer player) {
		for(String message : messages) {
			BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
			player.sendMessage(components);
		}
	}
}
