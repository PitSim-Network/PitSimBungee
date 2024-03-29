package net.pitsim.bungee.pitsim;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.discord.AuthenticationManager;
import net.pitsim.bungee.discord.DiscordManager;
import net.pitsim.bungee.discord.DiscordUser;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.objects.Leaderboard;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PlayerData;
import net.pitsim.bungee.objects.ServerType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

//This is disabled
public class LockdownManager implements Listener {
	public static List<ProxiedPlayer> captchaPlayers = new ArrayList<>();
	public static Map<ProxiedPlayer, UUID> captchaAnswers = new HashMap<>();
	private static boolean requireVerification = false;
	private static boolean requireCaptcha = false;
	public static String verificationMessage = "&c&lVERIFICATION! &7Discord verification is required to join";

	public static final int MINUTES_TO_PASS = 120;

	public static void sendCaptchaMessage(ProxiedPlayer player) {
		if(!captchaAnswers.containsKey(player)) captchaAnswers.put(player, UUID.randomUUID());
		TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c&lCAPTCHA! &7Click this to complete"));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/captcha " + captchaAnswers.get(player)));
		player.sendMessage(message);
	}

	@EventHandler
	public void onCommand(ChatEvent event) {
		if(event.getMessage().toLowerCase().startsWith("/captcha") || event.getMessage().toLowerCase().startsWith("/disc"))
			return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if(!isVerified(player)) {
			event.setCancelled(true);
			AOutput.error(player, verificationMessage);
			AuthenticationManager.attemptAuthentication(player);
			return;
		}
		if(!isCaptcha(player)) {
			event.setCancelled(true);
			sendCaptchaMessage(player);
		}
	}

	@EventHandler
	public void onQuit(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		captchaPlayers.remove(player);
		captchaAnswers.remove(player);
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if(getPlaytime(player) >= MINUTES_TO_PASS) return;

		((ProxyRunnable) () -> {
			if(!isCaptcha(player)) {
				sendCaptchaMessage(player);
				sendCaptchaMessage(player);
				sendCaptchaMessage(player);
			}
		}).runAfter(500, TimeUnit.MILLISECONDS);
	}

	public static boolean isVerified(ProxiedPlayer player) {
		if(!requireVerification || player.hasPermission("pitsim.autoverify")) return true;
		if(getPlaytime(player) >= MINUTES_TO_PASS) return true;

		DiscordUser discordUser = DiscordManager.getUser(player.getUniqueId());
		return discordUser != null && discordUser.wasAuthenticatedRecently();
	}

	public static boolean isCaptcha(ProxiedPlayer player) {
		if(!requireCaptcha || player.hasPermission("pitsim.autoverify")) return true;
		return getPlaytime(player) >= MINUTES_TO_PASS || captchaPlayers.contains(player);
	}

	public static boolean verificationRequired() {
		return requireVerification;
	}

	public static boolean captchaRequired() {
		return requireCaptcha;
	}

	public static void enableVerification() {
		if(requireVerification) return;
		requireVerification = true;
		purgeLobbies();
	}

	public static void disableVerification() {
		if(!requireVerification) return;
		requireVerification = false;
	}

	public static void enableCaptcha() {
		if(requireCaptcha) return;
		requireCaptcha = true;
		purgeLobbies();
	}

	public static void disableCaptcha() {
		if(!requireCaptcha) return;
		requireCaptcha = false;
	}

	public static void passCaptcha(ProxiedPlayer player) {
		captchaPlayers.add(player);
		AOutput.color(player, "&a&lSUCCESS! &7Captcha Passed!");

		PitSimServerManager overworld = PitSimServerManager.getManager(ServerType.OVERWORLD);
		assert overworld != null;
		overworld.queue(player, 0, false);
	}

	public static double getPlaytime(ProxiedPlayer player) {
		PlayerData data = PlayerData.getPlayerData(player.getUniqueId());
		if(data == null) return 0;
		return data.getData(Leaderboard.PLAYTIME);
	}

	public static boolean canJoin(ProxiedPlayer player) {
		if(requireVerification) {
			boolean verified = isVerified(player);
			if(!verified) {
				AOutput.color(player, verificationMessage);
				return false;
			}
		}
		if(requireCaptcha) {
			boolean captcha = isCaptcha(player);
			if(!captcha) {
				sendCaptchaMessage(player);
				return false;
			}
		}
		return true;
	}

	public static void purgeLobbies() {
		for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
			for(ProxiedPlayer player : pitSimServer.getPlayers()) {
				if(!canJoin(player)) player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
			}
		}
	}
}
