package dev.wiji.instancemanager;


import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.alogging.ConnectionManager;
import dev.wiji.instancemanager.alogging.LogManager;
import dev.wiji.instancemanager.aserverstatistics.StatisticsManager;
import dev.wiji.instancemanager.auctions.AuctionManager;
import dev.wiji.instancemanager.auctions.AuctionMessaging;
import dev.wiji.instancemanager.commands.*;
import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.market.MarketManager;
import dev.wiji.instancemanager.market.MarketMessaging;
import dev.wiji.instancemanager.objects.ServerType;
import dev.wiji.instancemanager.pitsim.*;
import dev.wiji.instancemanager.skywars.PitsimQuestManager;
import dev.wiji.instancemanager.skywars.PluginMessageSender;
import dev.wiji.instancemanager.skywars.SkywarsGameManager;
import dev.wiji.instancemanager.skywars.SkywarsPluginListener;
import dev.wiji.instancemanager.storage.EditSessionManager;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.dupe.DupeManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryMessenger;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeMain extends Plugin {
	public static BungeeMain INSTANCE;
	public static PteroApplication api = PteroBuilder.createApplication("***REMOVED***",
			"***REMOVED***");
	public static PteroClient client = PteroBuilder.createClient("***REMOVED***",
			"***REMOVED***");

	public static long STARTUP_TIME;
	public static final ZoneId TIME_ZONE = ZoneId.of("America/New_York");

	public static LuckPerms LUCKPERMS;

	@Override
	public void onEnable() {
		INSTANCE = this;
		ConfigManager.onEnable();
		TableManager.registerTables();

		LUCKPERMS = LuckPermsProvider.get();
		this.getProxy().registerChannel("BungeeCord");
		STARTUP_TIME = System.currentTimeMillis();
		FirestoreManager.init();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			e.printStackTrace();
		}

		MarketManager.init();

//		getProxy().getPluginManager().registerListener(BungeeMain.INSTANCE, new DiscordManager());
		if(!ConfigManager.isDev()) getProxy().getPluginManager().registerListener(BungeeMain.INSTANCE, new DiscordManager());
		getProxy().getPluginManager().registerListener(this, new SkywarsPluginListener());
		getProxy().getPluginManager().registerListener(this, new PluginMessageManager());
		getProxy().getPluginManager().registerListener(this, new MessageListener());
		getProxy().getPluginManager().registerListener(this, new ServerDataManager());
		getProxy().getPluginManager().registerListener(this, new ServerChangeListener());
		getProxy().getPluginManager().registerListener(this, new LogManager());
		getProxy().getPluginManager().registerListener(this, new ConnectionManager());
		getProxy().getPluginManager().registerListener(this, new StorageManager());
		getProxy().getPluginManager().registerListener(this, new EditSessionManager());
		getProxy().getPluginManager().registerListener(this, new IdentificationManager());
		getProxy().getPluginManager().registerListener(this, new CrossServerMessageManager());
		getProxy().getPluginManager().registerListener(this, new PlayerManager());
		getProxy().getPluginManager().registerListener(this, new PitsimQuestManager());
		getProxy().getPluginManager().registerListener(this, new LockdownManager());
		getProxy().getPluginManager().registerListener(this, new MarketMessaging());
		getProxy().getPluginManager().registerListener(this, new MarketManager());
		getProxy().getPluginManager().registerListener(this, new CommandBlocker());
		getProxy().getPluginManager().registerListener(this, new AuthenticationManager());
		getProxy().getPluginManager().registerListener(this, new StatisticsManager());
		getProxy().getPluginManager().registerListener(this, new AuctionMessaging());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new DupeManager());
		ConfigManager.getMiniServerList();

		ServerManager.onEnable();
		SkywarsGameManager.fetchServer();
		PluginMessageSender.sendPlayerStats();
		QueryMessenger messenger = PluginQuery.getMessenger();
		messenger.getEventBus().registerListener(new PluginMessageManager());

		getProxy().getPluginManager().registerCommand(this, new PlayCommand(this));
		getProxy().getPluginManager().registerCommand(this, new ToggleCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LobbyCommand(this));
		getProxy().getPluginManager().registerCommand(this, new DevCommand(this));
		getProxy().getPluginManager().registerCommand(this, new AdminCommand(this));
		getProxy().getPluginManager().registerCommand(this, new PTestCommand(this));
		getProxy().getPluginManager().registerCommand(this, new KyroCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LobbiesCommand(this));
		getProxy().getPluginManager().registerCommand(this, new BroadcastCommand(this));
		getProxy().getPluginManager().registerCommand(this, new ServerJoinCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LockdownCommand());
		getProxy().getPluginManager().registerCommand(this, new CaptchaCommand());
		getProxy().getPluginManager().registerCommand(this, new SeenCommand());
		getProxy().getPluginManager().registerCommand(this, new DupeCheckCommand());
		getProxy().getPluginManager().registerCommand(this, new BackupCommand());
		getProxy().getPluginManager().registerCommand(this, new LinkCommand());
		getProxy().getPluginManager().registerCommand(this, new UnlinkCommand());
		getProxy().getPluginManager().registerCommand(this, new ViewCommand());
//		getProxy().getPluginManager().registerCommand(this, new MigrateCommand());

		ConfigManager.getPitSimServerList();
		ConfigManager.getDarkzoneServerList();

		new PitSimServerManager(ServerType.OVERWORLD, 8 ,4);
		new PitSimServerManager(ServerType.DARKZONE, 8 ,4);

		RestartManager.init();
		AuctionManager.init();

		ArcticGuilds.onEnable(this);
	}

	@Override
	public void onDisable() {
		//make sure to unregister the registered channels in case of a reload
		this.getProxy().unregisterChannel("BungeeCord");
		ConfigManager.save();
		MarketManager.shutdown();

		if(FirestoreManager.registration != null) FirestoreManager.registration.remove();

		DiscordManager.disable();
		ArcticGuilds.onDisable(this);
	}

	public static String getName(UUID uuid, boolean printError) {
		return IdentificationManager.getUsername(uuid);
	}

	public static List<ProxiedPlayer> getMainGamemodePlayers() {
		List<ProxiedPlayer> players = new ArrayList<>();
		for(ProxiedPlayer player : INSTANCE.getProxy().getPlayers()) {
			String server = player.getServer().getInfo().getName();
			if(server.contains("pitsim") || server.contains("darkzone")) players.add(player);
		}
		return players;
	}

	public static UUID getUUID(String name, boolean printError) {
		return IdentificationManager.getUUID(name);
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
