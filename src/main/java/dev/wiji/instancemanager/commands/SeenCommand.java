package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.SQL.Constraint;
import dev.wiji.instancemanager.SQL.Field;
import dev.wiji.instancemanager.SQL.SQLTable;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.pitsim.IdentificationManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SeenCommand extends Command {
	public SeenCommand() {
		super("seen");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!player.hasPermission("pitsim.admin")) return;

		if(args.length == 0) {
			AOutput.error(player, "&cUsage: /seen <player/UUID>");
			return;
		}

		UUID target;

		try {
			target = UUID.fromString(args[0]);
		} catch (IllegalArgumentException e) {
			ProxiedPlayer targetPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(args[0]);
			if(targetPlayer == null) {

				UUID playerUUID = BungeeMain.getUUID(args[0], false);
				if(playerUUID == null) {
					AOutput.error(player, "&cPlayer not found.");
					return;
				} else target = playerUUID;
			} else target = targetPlayer.getUniqueId();
		}

		ProxiedPlayer targetPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(target);
		if(targetPlayer != null) {
			ServerInfo server = targetPlayer.getServer().getInfo();
			AOutput.color(player, "&a" + targetPlayer.getName() + " &7is currently connected to &6" + server.getName() + "&7.");
			return;
		}

		SQLTable table = TableManager.getTable(IdentificationManager.TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Field("username"),
				new Field("last_join"),
				new Constraint("uuid", target.toString())
		);

		try {
			if(rs.next()) {

				String username = rs.getString("username");
				long lastJoin = rs.getLong("last_join");

				AOutput.color(player, "&c" + username + " &7was last seen &6" + Misc.longToTimeFormatted(System.currentTimeMillis() - lastJoin) + " &7ago.");
			} else {
				AOutput.error(player, "&cPlayer not found. (Try UUID?)");
			}

			rs.close();
		} catch(SQLException e) { throw new RuntimeException(e); }
	}
}
