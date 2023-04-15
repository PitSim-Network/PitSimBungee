package dev.wiji.instancemanager.discord.commands;

import dev.wiji.instancemanager.discord.Constants;
import dev.wiji.instancemanager.discord.DiscordCommand;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.storage.dupe.DiscordWebhook;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.IOException;

public class SpamWijiCommand extends DiscordCommand {
	public SpamWijiCommand() {
		super("spam");
		enabled = false;
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "(KYRO COMMAND) spams wiji").addOptions(
				new OptionData(OptionType.STRING, "message", "the message to spam wiji with", true)
		);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(event.getMember().getIdLong() != Constants.KYRO_ID) {
			event.reply("You are not kyro").setEphemeral(true).queue();
			return;
		}

		event.reply("as you wish :o").queue();
		String message = event.getOption("message").getAsString();
		new Thread(() -> {
			spamGuild(event.getGuild(), event.getChannel(), DiscordManager.MAIN_GUILD, message);
			spamGuild(event.getGuild(), event.getChannel(), DiscordManager.PRIVATE_GUILD, message);
			spamGuild(event.getGuild(), event.getChannel(), DiscordManager.CONTRIVANCE_GUILD, message);
		}).start();
	}

	public static void spamGuild(Guild initialGuild, MessageChannel initialChannel, Guild guild, String message) {
		for(TextChannel textChannel : guild.getTextChannels()) {
			Webhook webhook = textChannel.createWebhook("PitSim").complete();

			DiscordWebhook webhookRequest = new DiscordWebhook(webhook.getUrl());
			webhookRequest.setContent("<@741455066795343932> " + message);
			webhookRequest.setAvatarUrl("https://cdn.discordapp.com/avatars/841567626466951171/b6910f2d318f6ca120282749a84868b8.png?size=2048");

			try {
				webhookRequest.execute();
			} catch(IOException exception) {
				exception.printStackTrace();
			}

			webhook.delete().complete();
			if(guild == DiscordManager.MAIN_GUILD || initialGuild != DiscordManager.MAIN_GUILD)
				initialChannel.sendMessage("Pinged wiji in: `" + textChannel.getName() + "`").complete();
			sleep(5_000);
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
