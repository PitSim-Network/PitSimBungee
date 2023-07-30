package net.pitsim.bungee;


import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.pitsim.bungee.alogging.ConnectionManager;
import net.pitsim.bungee.alogging.LogManager;
import net.pitsim.bungee.aserverstatistics.StatisticsManager;
import net.pitsim.bungee.auctions.AuctionManager;
import net.pitsim.bungee.auctions.AuctionMessaging;
import net.pitsim.bungee.commands.*;
import net.pitsim.bungee.discord.AuthenticationManager;
import net.pitsim.bungee.discord.DiscordManager;
import net.pitsim.bungee.guilds.ArcticGuilds;
import net.pitsim.bungee.market.MarketManager;
import net.pitsim.bungee.market.MarketMessaging;
import net.pitsim.bungee.objects.ServerType;
import net.pitsim.bungee.pitsim.*;
import net.pitsim.bungee.skywars.PitsimQuestManager;
import net.pitsim.bungee.skywars.PluginMessageSender;
import net.pitsim.bungee.skywars.SkywarsGameManager;
import net.pitsim.bungee.skywars.SkywarsPluginListener;
import net.pitsim.bungee.storage.EditSessionManager;
import net.pitsim.bungee.storage.StorageManager;
import net.pitsim.bungee.storage.dupe.DupeManager;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryMessenger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BungeeMain extends Plugin {
	public static BungeeMain INSTANCE;
	public static PteroApplication api;
	public static PteroClient client;

	public static long STARTUP_TIME;
	public static final ZoneId TIME_ZONE = ZoneId.of("America/New_York");

	public static LuckPerms LUCKPERMS;

	@Override
	public void onEnable() {
		INSTANCE = this;
		ConfigManager.onEnable();

		api = PteroBuilder.createApplication(ConfigManager.get("ptero-url"),
				ConfigManager.get("ptero-api-key"));
		client = PteroBuilder.createClient(ConfigManager.get("ptero-url"),
				ConfigManager.get("ptero-client-key"));

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
		try {
			Connection connection = IdentificationManager.getConnection();
			String name = IdentificationManager.getUsername(Objects.requireNonNull(connection), uuid);
			connection.close();
			return name;

		} catch(SQLException throwables) {
			if(printError) throwables.printStackTrace();
		}
		return null;
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
		try {
			Connection connection = IdentificationManager.getConnection();
			UUID uuid = IdentificationManager.getUuid(Objects.requireNonNull(connection), name);
			connection.close();
			return uuid;

		} catch(SQLException throwables) {
			if(printError) throwables.printStackTrace();
		}
		return null;
	}
}
