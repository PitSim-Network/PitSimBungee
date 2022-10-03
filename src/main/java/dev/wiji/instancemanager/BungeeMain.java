package dev.wiji.instancemanager;


import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import dev.wiji.instancemanager.Commands.BetaCommad;
import dev.wiji.instancemanager.Commands.LobbyCommand;
import dev.wiji.instancemanager.Commands.PlayCommand;
import dev.wiji.instancemanager.Commands.ToggleCommand;
import dev.wiji.instancemanager.PitSim.MessageListener;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import dev.wiji.instancemanager.PitSim.PluginMessageManager;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {
	public static BungeeMain INSTANCE;
	public static PteroApplication api = PteroBuilder.createApplication("***REMOVED***",
			"ePKZBUXCVt1gS42MHwh20MfD5vreObm9JFNVCo788eV0ROnr");
	public static PteroClient client = PteroBuilder.createClient("***REMOVED***",
			"im4F1vVHTJKIjhRQcvJ8CAdOX3aCt99JmpukhFGbzQXI5BOQ");

	@Override
	public void onEnable() {
		INSTANCE = this;
		this.getProxy().registerChannel("BungeeCord");
//		getProxy().getPluginManager().registerListener(this, new SkywarsPluginListener());
		getProxy().getPluginManager().registerListener(this, new PluginMessageManager());
		getProxy().getPluginManager().registerListener(this, new MessageListener());
		ConfigManager.onEnable();
		ConfigManager.getMiniServerList();
//		ServerManager.onEnable();
//		SkywarsGameManager.fetchServer();
//		PluginMessageSender.sendPlayerStats();
		getProxy().getPluginManager().registerCommand(this, new PlayCommand(this));
		getProxy().getPluginManager().registerCommand(this, new ToggleCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LobbyCommand(this));
		getProxy().getPluginManager().registerCommand(this, new BetaCommad(this));

		ConfigManager.getPitSimServerList();
		PitSimServerManager.init();
	}

	@Override
	public void onDisable() {
		//make sure to unregister the registered channels in case of a reload
		this.getProxy().unregisterChannel("BungeeCord");
		ConfigManager.onDisable();
	}

//	public void createServer() {
//
//		PteroApplication api = PteroBuilder.createApplication("***REMOVED***", "ePKZBUXCVt1gS42MHwh20MfD5vreObm9JFNVCo788eV0ROnr");
//		api.retrieveUsers().executeAsync(users -> users.forEach(u -> System.out.println(u.getFullName())));
//
//
//		Nest nest = api.retrieveNestById("1").execute();
//		Location location = api.retrieveLocationById("1").execute();
//		ApplicationEgg egg = api.retrieveEggById(nest, "3").execute();
//
//		Map<String, EnvironmentValue<?>> map = new HashMap<>();
//		map.put("SERVER_JARFILE", EnvironmentValue.ofString("server.jar"));
//		map.put("VERSION", EnvironmentValue.ofString("1.8.8"));
//
//		PteroAction<ApplicationServer> action = api.createServer()
//				.setName("Mini01")
//				.setOwner(api.retrieveUserById("1").execute())
//				.setEgg(egg)
//				.setLocation(location)
//				.setAllocations(1L)
//				.setDatabases(0L)
//				.setCPU(50L)
//				.setDisk(3L, DataType.GB)
//				.setMemory(1L, DataType.GB)
//				.setPort(25802)
//				.startOnCompletion(false)
//				.setEnvironment(map);
//		ApplicationServer server = action.execute();
//	}
}
