//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ALoreBuilder {
    private final List<String> lore = new ArrayList<>();

    public ALoreBuilder() {
    }

    public ALoreBuilder(List<String> lines) {
        this.addLore(lines);
    }

    public ALoreBuilder(String... lines) {
        this.addLore(lines);
    }

    public ALoreBuilder(DummyItemStack itemStack) {
        if(itemStack.getLore() != null) {
            this.addLore(itemStack.getLore());
        }
    }

    public ALoreBuilder addLore(List<String> lines) {
        this.lore.addAll(lines);
        return this;
    }

    public ALoreBuilder addLore(String... lines) {
        this.lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ALoreBuilder border(String border) {
        this.lore.add(0, border);
        this.lore.add(border);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public ALoreBuilder colorize() {
        for(int i = 0; i < this.lore.size(); ++i) {
            String line = (String)this.lore.get(i);
            this.lore.set(i, ChatColor.translateAlternateColorCodes('&', line));
        }

        return this;
    }

    public List<String> getLore() {
        return this.colorize().lore;
    }
}
