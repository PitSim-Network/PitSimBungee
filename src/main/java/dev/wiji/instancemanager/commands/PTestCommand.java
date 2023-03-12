package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.discord.DiscordUser;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.TokensResponse;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.UUID;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!player.hasPermission("pitsim.admin")) return;

//		09474ee7-b7da-49b4-9d93-770550b686a4
		DiscordUser discordUser = DiscordManager.getUser(UUID.fromString(args[0]));
		System.out.println(discordUser.refreshToken);

		try {
			DiscordOAuth oauthHandler = new DiscordOAuth(AuthenticationManager.CLIENT_ID, AuthenticationManager.OAUTH_SECRET,
					"http://147.135.8.130:3000", new String[] {"identify", "guilds.join"});
			System.out.println(oauthHandler);
			TokensResponse tokens = oauthHandler.refreshTokens(discordUser.refreshToken);
			System.out.println(tokens.getRefreshToken());
			System.out.println(tokens.getAccessToken());
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
