package dev.wiji.instancemanager.alogging;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ServerLogManager implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();

		if(strings.get(0).equals("LOG")) {
			String serverName = strings.get(1);
			String logMessage = strings.get(2);
			System.out.println(logMessage);

			PrintWriter writer;
			try {
				writer = new PrintWriter(BungeeMain.INSTANCE.getDataFolder() + "/test.log", "UTF-8");
			} catch(FileNotFoundException | UnsupportedEncodingException exception) {
				throw new RuntimeException(exception);
			}
			writer.println(logMessage);
			writer.close();
		}
	}
}
