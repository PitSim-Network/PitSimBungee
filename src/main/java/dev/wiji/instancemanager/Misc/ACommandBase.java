//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class ACommandBase extends Command {
    private final String executor;
    private final boolean isBase;

    public ACommandBase(String executor) {
        super(executor);
        this.executor = executor;
        this.isBase = true;
        BungeeMain.INSTANCE.getProxy().getPluginManager().registerCommand(BungeeMain.INSTANCE, this);
    }

    public ACommandBase(AMultiCommand base, String executor) {
        super(executor);
        this.executor = executor;
        this.isBase = false;
        base.registerCommand(this);
    }

    public void execute(CommandSender sender, String[] args) {
        execute(sender, new ArrayList<>(Arrays.asList(args)));
    }

    public abstract void execute(CommandSender sender, List<String> args);

    public String getExecutor() {
        return this.executor;
    }

    public boolean isBase() {
        return this.isBase;
    }

}
