package dev.wiji.instancemanager.guilds.commands.guildcommands;

import dev.wiji.instancemanager.guilds.controllers.ChatManager;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.misc.ACommand;
import dev.wiji.instancemanager.misc.AMultiCommand;
import dev.wiji.instancemanager.misc.AOutput;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class ChatCommand extends ACommand {
	public ChatCommand(AMultiCommand base, String executor) {
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

		if(ChatManager.guildChatPlayer.contains(player.getUniqueId())) {
			ChatManager.guildChatPlayer.remove(player.getUniqueId());
			AOutput.color(player, "&a&lGUILD! &7Guild chat &cdisabled");
		} else {
			ChatManager.guildChatPlayer.add(player.getUniqueId());
			AOutput.color(player, "&a&lGUILD! &7Guild chat &aenabled");
		}
	}
}
