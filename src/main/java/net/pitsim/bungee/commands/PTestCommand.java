package net.pitsim.bungee.commands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.SQL.TableManager;
import net.pitsim.bungee.guilds.GuildMessaging;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import net.pitsim.bungee.storage.StorageManager;
import net.pitsim.bungee.storage.StorageProfile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if(!player.hasPermission("pitsim.admin")) return;


			AOutput.log("There are " + TableManager.openResultSets + "/" + TableManager.totalResultSets + " result sets open");
		}
	}
}
