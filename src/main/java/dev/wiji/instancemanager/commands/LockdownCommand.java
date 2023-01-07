package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.pitsim.LockdownManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LockdownCommand extends Command {
	public LockdownCommand() {
		super("lockdown");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(args.length < 1) {
			AOutput.color(player, "&c&lLOCKDOWN! &7Verification currently " + getText(LockdownManager.verificationRequired()));
			AOutput.color(player, "&c&lLOCKDOWN! &7Captcha currently " + getText(LockdownManager.captchaRequired()));
			return;
		}
		String type = args[0].toLowerCase();

		if(type.equals("verification") || type.equals("verify")) {
			if(args.length < 2) {
				AOutput.color(player, "&c&lLOCKDOWN! &7Verification currently " + getText(LockdownManager.verificationRequired()));
				AOutput.color(player, "&c&lLOCKDOWN! &7Captcha currently " + getText(LockdownManager.captchaRequired()));
				return;
			}
			String enabled = args[1].toLowerCase();

			if(enabled.equals("on") || enabled.equals("yes")) {
				if(LockdownManager.verificationRequired()) {
					AOutput.error(player, "&c&lLOCKDOWN! &7Verification already enabled");
					return;
				}
				LockdownManager.enableVerification();
				AOutput.error(player, "&c&lLOCKDOWN! &7Verification enabled");
			} else if(enabled.equals("off") || enabled.equals("no")) {
				if(!LockdownManager.verificationRequired()) {
					AOutput.color(player, "&c&lLOCKDOWN! &7Verification not enabled");
					return;
				}
				LockdownManager.disableVerification();
				AOutput.color(player, "&c&lLOCKDOWN! &7Verification disabled");
			} else {
				AOutput.color(player, "&c&lLOCKDOWN! &7verify <on/off>");
			}
		} else if(type.equals("captcha")) {
			if(args.length < 2) {
				AOutput.color(player, "&c&lLOCKDOWN! &7Verification currently " + getText(LockdownManager.verificationRequired()));
				AOutput.color(player, "&c&lLOCKDOWN! &7Captcha currently " + getText(LockdownManager.captchaRequired()));
				return;
			}
			String enabled = args[1].toLowerCase();

			if(enabled.equals("on") || enabled.equals("yes")) {
				if(LockdownManager.captchaRequired()) {
					AOutput.color(player, "&c&lLOCKDOWN! &7Captcha already enabled");
					return;
				}
				LockdownManager.enableCaptcha();
				AOutput.color(player, "&c&lLOCKDOWN! &7Captcha enabled");
			} else if(enabled.equals("off") || enabled.equals("no")) {
				if(!LockdownManager.captchaRequired()) {
					AOutput.color(player, "&c&lLOCKDOWN! &7Captcha not enabled");
					return;
				}
				LockdownManager.disableCaptcha();
				AOutput.color(player, "&c&lLOCKDOWN! &7Captcha disabled");
			} else {
				AOutput.color(player, "&c&lLOCKDOWN! &7captcha <on/off>");
			}
		} else {
			AOutput.error(player, "&c&lLOCKDOWN! &7<verify|captcha> <on/off>");
		}
	}

	public static String getText(boolean value) {
		return value ? "&a&lON" : "&c&lOFF";
	}
}
