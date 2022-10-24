package dev.wiji.instancemanager.Misc;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AMessageBuilder {
    private List<String> message = new ArrayList();

    public AMessageBuilder() {
    }

    public AMessageBuilder addLines(List<String> lines) {
        this.message.addAll(lines);
        return this;
    }

    public AMessageBuilder addLine(String... lines) {
        this.message.addAll(Arrays.asList(lines));
        return this;
    }

    public AMessageBuilder border(String border) {
        this.message.add(0, border);
        this.message.add(border);
        return this;
    }

    public AMessageBuilder colorize() {
        for(int i = 0; i < this.message.size(); ++i) {
            String line = (String)this.message.get(i);
            this.message.set(i, ChatColor.translateAlternateColorCodes('&', line));
        }

        return this;
    }

    public List<String> getMessage() {
        return this.colorize().message;
    }

    public void send(ProxiedPlayer player) {
        Iterator var2 = this.message.iterator();

        while(var2.hasNext()) {
            String line = (String)var2.next();
            BaseComponent[] components = net.md_5.bungee.api.chat.TextComponent.fromLegacyText(line);
            player.sendMessage(components);
        }

    }
}
