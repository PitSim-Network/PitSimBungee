package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Objects.PluginMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PitSimListener implements Listener {

    @EventHandler
    public void onMessage(MessageEvent event) {
        System.out.println("Message received!");
        PluginMessage message = event.getMessage();

        System.out.println(message.getStrings().get(0));
        System.out.println(message.getIntegers().get(0));
        System.out.println(message.getBooleans().get(0));
    }
}
