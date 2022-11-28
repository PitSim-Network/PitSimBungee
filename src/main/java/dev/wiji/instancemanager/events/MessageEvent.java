package dev.wiji.instancemanager.events;

import dev.wiji.instancemanager.objects.PluginMessage;
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
