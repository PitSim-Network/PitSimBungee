package dev.wiji.instancemanager.discord;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public abstract class DiscordCommand {
	public String name;
	public boolean requireVerification;
	public boolean enabled = true;

	public DiscordCommand(String name) {
		this.name = name;
	}

	public abstract SlashCommandData getCommandStructure();
	public abstract void execute(SlashCommandInteractionEvent event);

	public List<Command.Choice> autoComplete(CommandAutoCompleteInteractionEvent event, String currentOption, String currentValue) {
		return new ArrayList<>();
	}
}
