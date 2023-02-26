package dev.wiji.instancemanager.commands;

import dev.kyro.pitsim.controllers.AuthenticationManager;
import dev.wiji.instancemanager.discord.AuthenticationManager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bukkit.entity.Player;

public class LinkCommand extends Command {

	public LinkCommand() {
		super("link");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;


	}
}
