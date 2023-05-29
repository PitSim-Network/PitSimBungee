package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.SQL.Constraint;
import dev.wiji.instancemanager.SQL.Field;
import dev.wiji.instancemanager.SQL.SQLTable;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.discord.commands.CleanCommand;
import dev.wiji.instancemanager.discord.commands.GraphCommand;
import dev.wiji.instancemanager.discord.commands.PingCommand;
import dev.wiji.instancemanager.discord.commands.SpamWijiCommand;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.PrivateInfo;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordManager implements EventListener, Listener {
	public static JDABuilder BUILDER;
	public static JDA JDA;
	public static List<DiscordCommand> commands = new ArrayList<>();
	public static boolean isEnabled;
	public static boolean commandsEnabled = true;

	public static Guild MAIN_GUILD;
	public static Guild PRIVATE_GUILD;
	public static Guild CONTRIVANCE_GUILD;

	public static final String DISCORD_TABLE = "DiscordAuthentication";

	public DiscordManager() {
		AOutput.log("Discord bot loading");
		isEnabled = true;

		BUILDER = JDABuilder.createDefault(PrivateInfo.BOT_TOKEN);
		try {
			BUILDER.setMemberCachePolicy(MemberCachePolicy.ALL);
			BUILDER.enableIntents(GatewayIntent.GUILD_MEMBERS);
			BUILDER.addEventListeners(this);
//			BUILDER.addEventListeners(new DeploymentManager());
			JDA = BUILDER.build();
			JDA.awaitReady();
		} catch(LoginException | InterruptedException e) {
			e.printStackTrace();
		}

		MAIN_GUILD = JDA.getGuildById(Constants.MAIN_GUILD_ROLE_ID);
		PRIVATE_GUILD = JDA.getGuildById(Constants.PRIVATE_GUILD_ID);
		CONTRIVANCE_GUILD = JDA.getGuildById(Constants.CONTRIVANCE_GUILD_ID);

//		Random Things
		new InGameNitro();

		registerCommands();
		setupSlashCommands();

		MAIN_GUILD.retrieveCommands().queue(currentCommands -> {
			for(DiscordCommand discordCommand : commands) {
				if(discordCommand.enabled) continue;
				for(Command command : currentCommands) {
					if(command.getApplicationIdLong() != Constants.BOT_ID || !discordCommand.name.equals(command.getName())) continue;
					MAIN_GUILD.deleteCommandById(command.getId()).queue();
					AOutput.log("Deleted Command: " + command.getName());
				}
			}
		});
	}

	public static void registerCommands() {
		registerCommand(new PingCommand());
		registerCommand(new SpamWijiCommand());
		registerCommand(new CleanCommand());
		registerCommand(new GraphCommand());
	}

	public static void setupSlashCommands() {
		for(DiscordCommand command : commands) MAIN_GUILD.upsertCommand(command.getCommandStructure()).queue();
		for(DiscordCommand command : commands) PRIVATE_GUILD.upsertCommand(command.getCommandStructure()).queue();
	}

	public static void registerCommand(DiscordCommand command) {
		commands.add(command);
	}

	public static void disable() {
		if(JDA == null) return;
		JDA.shutdownNow();
	}

	public static boolean isBoosting(UUID uuid) {
		DiscordUser user = getUser(uuid);
		if(user == null) return false;
		long id = user.discordID;
		Member member = MAIN_GUILD.retrieveMemberById(id).complete();

		return member.getRoles().contains(InGameNitro.nitroRole);
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if(event instanceof ReadyEvent)
			AOutput.log("Discord bot enabled");

		if(event instanceof SlashCommandInteractionEvent)
			onSlashCommand((SlashCommandInteractionEvent) event);

		if(event instanceof CommandAutoCompleteInteractionEvent)
			onAutoComplete((CommandAutoCompleteInteractionEvent) event);
	}

	public void onSlashCommand(SlashCommandInteractionEvent event) {
		if(!commandsEnabled) return;
		String command = event.getName();
		for(DiscordCommand discordCommand : commands) {
			if(!discordCommand.name.equals(command)) continue;
			if(discordCommand.requireVerification) {
				DiscordUser discordUser = DiscordManager.getUser(event.getMember().getIdLong());
				if(discordUser == null || !discordUser.wasAuthenticatedRecently()) {
					event.reply("You must link your account on mc.pitsim.net with /link to use this command").setEphemeral(true).queue();
					return;
				}
			}
			discordCommand.execute(event);
			return;
		}
	}

	public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
		if(!commandsEnabled) return;
		String command = event.getName();
		String currentOption = event.getFocusedOption().getName();
		String currentValue = event.getFocusedOption().getValue();
		for(DiscordCommand discordCommand : commands) {
			if(!discordCommand.name.equals(command)) continue;
			List<Command.Choice> choices = discordCommand.autoComplete(event, currentOption, currentValue);
			event.replyChoices(choices.stream().limit(25).collect(Collectors.toList())).queue();
			return;
		}
	}

	public static DiscordUser getUser(UUID uuid) {
		SQLTable table = TableManager.getTable(DISCORD_TABLE);
		if(table == null) throw new RuntimeException("Discord table not found");

		ResultSet rs = table.selectRow(
				new Field("discord_id"),
				new Field("access_token"),
				new Field("refresh_token"),
				new Field("last_refresh"),
				new Field("last_link"),
				new Field("last_boosting_claim"),
				new Constraint("uuid", uuid.toString())
		);

		try {
			if(rs.next()) {
				long id = rs.getLong("discord_id");
				return getValues(id, rs, uuid);
			} else return null;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DiscordUser getUser(long discordID) {
		SQLTable table = TableManager.getTable(DISCORD_TABLE);
		if(table == null) throw new RuntimeException("Discord table not found");

		ResultSet rs = table.selectRow(
				new Field("uuid"),
				new Field("access_token"),
				new Field("refresh_token"),
				new Field("last_refresh"),
				new Field("last_link"),
				new Field("last_boosting_claim"),
				new Constraint("discord_id", discordID)
		);

		try {
			if(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				return getValues(discordID, rs, uuid);
			} else return null;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@NotNull
	private static DiscordUser getValues(long discordID, ResultSet rs, UUID uuid) throws SQLException {
		String access = rs.getString("access_token");
		String refresh = rs.getString("refresh_token");
		long refreshTime = rs.getLong("last_refresh");
		long lastLink = rs.getLong("last_link");
		long claim = rs.getLong("last_boosting_claim");

		rs.close();
		return new DiscordUser(uuid, discordID, access, refresh, refreshTime, lastLink, claim);
	}

	public static List<UUID> getAllDiscordUserUUIDs() {
		List<UUID> discordUsers = new ArrayList<>();
		SQLTable table = TableManager.getTable(DISCORD_TABLE);
		if(table == null) throw new RuntimeException("Discord table not found");

		ResultSet rs = table.selectRow(
				new Field("uuid")
		);

		try {
			while(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				discordUsers.add(uuid);
			}

			rs.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return discordUsers;
	}

	@EventHandler
	public void onConnect(PostLoginEvent event) {
		ProxiedPlayer proxiedPlayer = event.getPlayer();
		DiscordUser discordUser = getUser(proxiedPlayer.getUniqueId());
		if(discordUser != null && discordUser.wasAuthenticatedRecently()) return;
		((ProxyRunnable) () -> {
			if(!proxiedPlayer.isConnected()) return;
			AOutput.color(proxiedPlayer, "&9&lLINK!&7 Link your minecraft account to your discord account with /link for a free key");
		}).runAfter(1, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		if(strings.isEmpty()) return;

		if(!strings.get(0).equals("BOOSTER_CLAIM")) return;
		String serverName = strings.get(1);
		String playerUUID = strings.get(2);

		new PluginMessage().writeString("BOOSTER_CLAIM")
				.writeString(playerUUID)
				.writeBoolean(isBoosting(UUID.fromString(playerUUID)))
				.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(serverName))
				.send();
	}
}
