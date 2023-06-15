
package net.pitsim.bungee.alogging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class OldConnectionData {
	public Map<String, PlayerConnectionData> playerConnectionMap = new HashMap<>();

	public static class PlayerConnectionData {
		public String name;
		public String host;

		public PlayerConnectionData(String name, String host) {
			this.name = name;
			this.host = host;
		}
	}

	public void save() {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(this);
			FileWriter writer = new FileWriter(ConnectionManager.dataFile.toPath().toString());
			writer.write(json);
			writer.close();
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
