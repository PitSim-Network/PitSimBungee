package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.ConfigManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DiscordManager implements EventListener {

	public static JDABuilder BUILDER;
	public static net.dv8tion.jda.api.JDA JDA;
	public static List<DiscordCommand> commands = new ArrayList<>();
	public static String prefix = ".";

	public static Guild MAIN_GUILD;
	public static Guild PRIVATE_GUILD;

	public DiscordManager() {
		System.out.println("Discord bot loading");
		BUILDER = JDABuilder.createDefault("***REMOVED***");
		try {
			BUILDER.setMemberCachePolicy(MemberCachePolicy.ALL);
			BUILDER.enableIntents(GatewayIntent.GUILD_MEMBERS);
			BUILDER.addEventListeners(this);
			BUILDER.addEventListeners(new InGameNitro());
			JDA = BUILDER.build();
			JDA.awaitReady();
		} catch(LoginException | InterruptedException e) {
			e.printStackTrace();
		}

		MAIN_GUILD = JDA.getGuildById(Constants.MAIN_GUILD_ROLE_ID);
		PRIVATE_GUILD = JDA.getGuildById(Constants.PRIVATE_GUILD_ROLE_ID);
	}

	public static void registerCommand(DiscordCommand command) {

		commands.add(command);
	}

	public static void disable() {
		if(ConfigManager.isDev()) return;
		JDA.shutdownNow();
	}

	public static void onMessage(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		if(!message.getContentRaw().startsWith(prefix)) return;

		String content = message.getContentRaw().replaceFirst(prefix, "");
		List<String> args = new ArrayList<>(Arrays.asList(content.split(" ")));
		String command = args.remove(0).toLowerCase();

		for(DiscordCommand discordCommand : commands) {

			if(!discordCommand.command.equals(command) && !discordCommand.aliases.contains(command)) continue;

			discordCommand.execute(event, args);
			return;
		}
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {

		if(event instanceof ReadyEvent)
			System.out.println("Discord bot enabled");

		if(event instanceof GuildMessageReceivedEvent)
			onMessage((GuildMessageReceivedEvent) event);
	}

	//	TODO: wiji implement (return null if no user)
	public static DiscordUser getUser(UUID uuid) {
		return null;
	}

	//	TODO: wiji implement (return null if no user)
	public static DiscordUser getUser(long discordID) {
		return null;
	}
}
