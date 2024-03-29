package net.pitsim.bungee.guilds.commands.guildcommands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.guilds.controllers.GuildManager;
import net.pitsim.bungee.guilds.controllers.PermissionManager;
import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.pitsim.bungee.guilds.controllers.objects.GuildMember;
import net.pitsim.bungee.guilds.controllers.objects.GuildMemberInfo;
import net.pitsim.bungee.guilds.enums.GuildRank;
import net.pitsim.bungee.guilds.inventories.ConfirmationGUI;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.ACommand;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.AMultiCommand;
import net.pitsim.bungee.ProxyRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransferCommand extends ACommand {
	public TransferCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) {
			AOutput.error(player, "You are not in a guild");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(!guild.ownerUUID.equals(player.getUniqueId())) {
				AOutput.error(player, "you are not the owner of your guild");
				return;
			}
		}

		if(args.size() < 1) {
			AOutput.error(player, "Usage: /transfer <player>");
			return;
		}

		Map.Entry<GuildMember, GuildMemberInfo> entry = guild.getMember(player);
		Map.Entry<GuildMember, GuildMemberInfo> targetEntry = null;
		UUID target = BungeeMain.getUUID(args.get(0), false);

		if(target == null) {
			AOutput.error(player, "That player does not exist");
			return;
		}
		for(Map.Entry<GuildMember, GuildMemberInfo> memberEntry : guild.members.entrySet()) {
			if(!memberEntry.getKey().playerUUID.equals(target)) continue;
			targetEntry = memberEntry;
			break;
		}
		if(targetEntry == null) {
			AOutput.error(player, "That player is not in your guild");
			return;
		}

		if(!PermissionManager.isAdmin(player)) {
			if(target.equals(player.getUniqueId())) {
				AOutput.error(player, "You cannot promote yourself");
				return;
			}
		}

		ProxyRunnable transfer = () -> {
			Map.Entry<GuildMember, GuildMemberInfo> entry1 = guild.getMember(player);
			Map.Entry<GuildMember, GuildMemberInfo> targetEntry1 = guild.getMember(target);

			if(entry1 == null || targetEntry1 == null || entry1.getValue().rank != GuildRank.OWNER) {
				AOutput.error(player, "Something went wrong. Please try again");
				return;
			}

			entry1.getValue().rank = GuildRank.CO_OWNER;
			targetEntry1.getValue().rank = GuildRank.OWNER;

			guild.ownerUUID = target;
			guild.save();
			guild.broadcast("&a&lGUILD! &7Guild ownership has been transferred to " + args.get(0));
		};

		ALoreBuilder yesLore = new ALoreBuilder("&7Clicking here will transfer", "&7your guild to " + args.get(0));
		ALoreBuilder noLore = new ALoreBuilder("&7Click to cancel");

		new ConfirmationGUI(player, transfer, yesLore, noLore, "Transfer Guild").open();
	}
}
