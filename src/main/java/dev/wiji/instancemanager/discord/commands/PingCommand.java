package dev.wiji.instancemanager.discord.commands;

import dev.wiji.instancemanager.discord.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand extends DiscordCommand {
	public PingCommand() {
		super("ping");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "pong!");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.reply("pong!").queue();
	}
}
