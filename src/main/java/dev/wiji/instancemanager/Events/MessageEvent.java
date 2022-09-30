package dev.wiji.instancemanager.Events;

import dev.wiji.instancemanager.Objects.PluginMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Event;

public class MessageEvent extends Event {

	private PluginMessage message;
	private String subChannel;

	public MessageEvent(PluginMessage message, String subChannel) {
		this.message = message;
		this.subChannel = subChannel;
	}

	public PluginMessage getMessage() {
		return message;
	}

	public String getSubChannel() {
		return subChannel;
	}

}
