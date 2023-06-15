package net.pitsim.bungee.discord.commands;

import net.pitsim.bungee.discord.Constants;
import net.pitsim.bungee.discord.DiscordCommand;
import net.pitsim.bungee.discord.DiscordManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CleanCommand extends DiscordCommand {
	public CleanCommand() {
		super("clean");
		enabled = false;
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "(KYRO COMMAND) removes wiji spam messages");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(event.getMember().getIdLong() != Constants.KYRO_ID) {
			event.reply("You are not kyro").setEphemeral(true).queue();
			return;
		}

		event.reply("as you wish :o").queue();
		new Thread(() -> {
			cleanGuild(event.getChannel(), DiscordManager.MAIN_GUILD);
			cleanGuild(event.getChannel(), DiscordManager.PRIVATE_GUILD);
			cleanGuild(event.getChannel(), DiscordManager.CONTRIVANCE_GUILD);
		}).start();
	}

	public static void cleanGuild(MessageChannel initialChannel, Guild guild) {
		for(TextChannel textChannel : guild.getTextChannels()) {
			MessageHistory messageHistory = textChannel.getHistoryFromBeginning(10).complete();
			for(Message message : messageHistory.getRetrievedHistory()) {
				if(message.getAuthor().getIdLong() != Constants.BOT_ID && !message.isWebhookMessage()) continue;
				message.delete().complete();
				initialChannel.sendMessage("removed message in channel: `" + textChannel.getName() + "`").complete();
				sleep(1_500);
			}
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
