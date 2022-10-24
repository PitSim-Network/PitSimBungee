package dev.wiji.instancemanager;


import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import de.sumafu.PlayerStatus.PlayerNeverConnectedException;
import de.sumafu.PlayerStatus.PlayerStatus;
import de.sumafu.PlayerStatus.PlayerStatusAPI;
import dev.wiji.instancemanager.Commands.*;
import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.PitSim.*;
import net.md_5.bungee.api.plugin.Plugin;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryMessenger;

import java.sql.SQLException;
import java.util.UUID;

public class BungeeMain extends Plugin {
	public static BungeeMain INSTANCE;
	public static PteroApplication api = PteroBuilder.createApplication("***REMOVED***",
			"4t1LoF4HF8cPHCDhScaXKxQkMLwnXRVBqFKvl0cAbYB0btzI");
	public static PteroClient client = PteroBuilder.createClient("***REMOVED***",
			"VILPyBXQfdJUEJEd4HUUbvTKsyDfbVD8KjYTOk2DMmyPfxCD");

	public static long STARTUP_TIME;

	public static PlayerStatusAPI psApi;

	@Override
	public void onEnable() {
		INSTANCE = this;
		this.getProxy().registerChannel("BungeeCord");
		STARTUP_TIME = System.currentTimeMillis();
		FirestoreManager.init();

		 psApi = PlayerStatus.getAPI();

//		getProxy().getPluginManager().registerListener(this, new SkywarsPluginListener());
		getProxy().getPluginManager().registerListener(this, new PluginMessageManager());
		getProxy().getPluginManager().registerListener(this, new MessageListener());
		getProxy().getPluginManager().registerListener(this, new ServerDataManager());
		getProxy().getPluginManager().registerListener(this, new ServerChangeListener());
		ConfigManager.onEnable();
		ConfigManager.getMiniServerList();


		ServerManager.onEnable();
//		SkywarsGameManager.fetchServer();
//		PluginMessageSender.sendPlayerStats();
		QueryMessenger messenger = PluginQuery.getMessenger();
		messenger.getEventBus().registerListener(new PluginMessageManager());

		getProxy().getPluginManager().registerCommand(this, new PlayCommand(this));
		getProxy().getPluginManager().registerCommand(this, new ToggleCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LobbyCommand(this));
		getProxy().getPluginManager().registerCommand(this, new BetaCommad(this));
		getProxy().getPluginManager().registerCommand(this, new DevCommand(this));
		getProxy().getPluginManager().registerCommand(this, new AdminCommand(this));

		ConfigManager.getPitSimServerList();
		ConfigManager.getDarkzoneServerList();
		PitSimServerManager.init();
		DarkzoneServerManager.init();
		RestartManager.init();

		ArcticGuilds.onEnable(this);
	}

	@Override
	public void onDisable() {
		//make sure to unregister the registered channels in case of a reload
		this.getProxy().unregisterChannel("BungeeCord");
//		ConfigManager.onDisable();

		if(FirestoreManager.registration != null) {
			FirestoreManager.registration.remove();
		}

		ArcticGuilds.onDisable(this);
	}

	public static String getName(UUID uuid, boolean printError) {
		try {
			return psApi.getNameOfUuid(uuid);
		} catch(SQLException | PlayerNeverConnectedException e) {
			if(printError) throw new RuntimeException(e);
		}
		return null;
	}

	public static UUID getUUID(String name, boolean printError) {
		try {
			return psApi.getUuidOfName(name);
		} catch(SQLException | PlayerNeverConnectedException e) {
			if(printError) throw new RuntimeException(e);
		}
		return null;
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
