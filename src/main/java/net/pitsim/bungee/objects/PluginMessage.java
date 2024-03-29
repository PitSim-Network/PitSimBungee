package net.pitsim.bungee.objects;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.pitsim.PluginMessageManager;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PluginMessage {

	private final List<String> strings = new ArrayList<>();
	private final List<Integer> integers = new ArrayList<>();
	private final List<Long> longs = new ArrayList<>();
	private final List<Boolean> booleans = new ArrayList<>();

	private final List<ServerInfo> servers = new ArrayList<>();

	public UUID messageID;
	public UUID responseID;
	public PluginMessage requestMessage;
	public String originServer;

	public PluginMessage(DataInputStream data) throws IOException {

		messageID = UUID.fromString(data.readUTF());
		responseID = UUID.fromString(data.readUTF());
		originServer = data.readUTF();

		int stringCount = data.readInt();
		int integerCount = data.readInt();
		int longCount = data.readInt();
		int booleanCount = data.readInt();

		for(int i = 0; i < stringCount; i++) {
			String string = data.readUTF();
			strings.add(string);
		}

		for(int i = 0; i < integerCount; i++) {
			integers.add(data.readInt());
		}

		for(int i = 0; i < longCount; i++) {
			longs.add(data.readLong());
		}

		for(int i = 0; i < booleanCount; i++) {
			booleans.add(data.readBoolean());
		}
	}

	public PluginMessage() {
		messageID = UUID.randomUUID();
		responseID = null;
	}

	public PluginMessage writeString(String string) {
		strings.add(string == null ? "" : string);
		return this;
	}

	public PluginMessage writeInt(int integer) {
		integers.add(integer);
		return this;
	}

	public PluginMessage writeLong(long longValue) {
		longs.add(longValue);
		return this;
	}

	public PluginMessage writeBoolean(boolean bool) {
		booleans.add(bool);
		return this;
	}

	public PluginMessage addServer(ServerInfo server) {
		servers.add(server);
		return this;
	}

	public PluginMessage addServer(String server) {
		ServerInfo info = BungeeMain.INSTANCE.getProxy().getServerInfo(server);
		if(info == null) return this;
		servers.add(info);
		return this;
	}

	public PluginMessage send() {
		this.responseID = UUID.randomUUID();
		for(ServerInfo server : servers) {
			PluginMessageManager.sendMessage(this, server);
		}
		return this;
	}

	public List<String> getStrings() {
		return strings;
	}

	public List<Integer> getIntegers() {
		return integers;
	}

	public List<Long> getLongs() {
		return longs;
	}

	public List<Boolean> getBooleans() {
		return booleans;
	}

	public List<ServerInfo> getServers() {
		return servers;
	}

	public PluginMessage respond(PluginMessage message, ServerInfo server) {
		message.responseID = messageID;

		PluginMessageManager.sendMessage(message, server);
		return this;
	}

	public boolean isResponseTo(PluginMessage message) {
		return responseID.equals(message.messageID);
	}
}
