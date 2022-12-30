//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wiji.instancemanager.misc;

import dev.wiji.instancemanager.objects.MainGamemodeServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AMultiCommand extends ACommandBase {
	private final List<ACommandBase> subCommands = new ArrayList<>();

	public AMultiCommand(String executor) {
		super(executor);
	}

	public AMultiCommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

	protected void registerCommand(ACommandBase subCommand) {
		this.subCommands.add(subCommand);
	}

	public List<ACommandBase> getSubCommands() {
		return this.subCommands;
	}

	public void execute(CommandSender sender, List<String> args) {

		if(sender instanceof ProxiedPlayer) {
			String server = ((ProxiedPlayer) sender).getServer().getInfo().getName();
			if(!server.contains("pitsim") && !server.contains("darkzone")) {
				AOutput.error((ProxiedPlayer) sender, "This command is disabled on this server.");
				return;
			}
		}

		MainGamemodeServer.guildCooldown.put((ProxiedPlayer) sender, System.currentTimeMillis());

		if(args.isEmpty()) {
			if(sender instanceof ProxiedPlayer) {
				this.createHelp().send((ProxiedPlayer) sender);
			}

		} else {
			Iterator<ACommandBase> var5 = this.subCommands.iterator();

			ACommandBase subCommand;
			do {
				if(!var5.hasNext()) {
					if(sender instanceof ProxiedPlayer) {
						this.createHelp().send((ProxiedPlayer) sender);
					}

					return;
				}

				subCommand = var5.next();
			} while(!subCommand.getExecutor().equals(args.get(0)));

			args.remove(0);
			subCommand.execute(sender, args);
		}
	}


	protected AMessageBuilder createHelp() {
		AMessageBuilder helpMessage = new AMessageBuilder();
		helpMessage.addLine("&b&l/&8&m---------------&7[ &3&lHELP &7]&8&m---------------&b&l\\").colorize();

		for(ACommandBase subCommand : this.subCommands) {
			String command = "&7 - /&f" + this.getExecutor() + " &3&l" + subCommand.getExecutor();
			helpMessage.addLine(command);
		}

		helpMessage.addLine("&b&l\\&8&m--------------------------------------&b&l/").colorize();
		return helpMessage;
	}
}
