package net.pitsim.bungee.commands;

import net.pitsim.bungee.pitsim.LockdownManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

//This is disabled
public class CaptchaCommand extends Command {

	public CaptchaCommand() {
		super("captcha");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(LockdownManager.captchaPlayers.contains(player) || args.length < 1) return;
		try {
			UUID uuid = UUID.fromString(args[0]);
			if(!LockdownManager.captchaAnswers.get(player).equals(uuid)) return;
			LockdownManager.passCaptcha(player);
		} catch(Exception ignored) { }
	}
}
