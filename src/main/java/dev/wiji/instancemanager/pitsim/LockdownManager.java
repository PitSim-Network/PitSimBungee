package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.Leaderboard;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.PlayerData;
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

		return DiscordManager.getUser(player.getUniqueId()) != null;
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

		OverworldServerManager.queue(player, 0, false);
	}

	public static double getPlaytime(ProxiedPlayer player) {
		PlayerData data = PlayerData.getPlayerData(player.getUniqueId());
		if(data == null) return 0;
		return data.getData(Leaderboard.PLAYTIME);
	}

	public static boolean canJoin(ProxiedPlayer player) {
		boolean captcha = isCaptcha(player);
		boolean verified = isVerified(player);

		if(!captcha) sendCaptchaMessage(player);
		if(!verified) AOutput.color(player, verificationMessage);

		if(requireCaptcha && requireVerification) return captcha && verified;
		else if(requireVerification) return verified;
		else if(requireCaptcha) return captcha;
		return true;
	}

	public static void purgeLobbies() {
		for(MainGamemodeServer mainGamemodeServer : MainGamemodeServer.serverList) {
			for(ProxiedPlayer player : mainGamemodeServer.getPlayers()) {
				if(!canJoin(player)) player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
			}
		}
	}
}
