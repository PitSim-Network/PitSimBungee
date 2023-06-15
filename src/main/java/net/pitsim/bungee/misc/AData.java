package net.pitsim.bungee.misc;

import net.pitsim.bungee.BungeeMain;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AData {
	private final File dataFile;
	private Configuration configuration;
	private final String fileName;
	private final String path;

	public AData(String fileName) {
		this(fileName, "");
	}

	public AData(String fileName, String path) {
		this(fileName, path, true);
	}

	public AData(String fileName, String path, boolean saveResource) {
		this.fileName = fileName;
		this.path = path;
		this.dataFile = new File(BungeeMain.INSTANCE.getDataFolder() + "/" + path, fileName + ".yml");
		if(!this.dataFile.exists()) {
			createDataFile(fileName, path);
		}

		try {
			this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.dataFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		if(saveResource) {
			try {
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, this.dataFile);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	public void reloadDataFile() {
		if(!this.dataFile.exists()) {
			createDataFile(this.fileName, this.path);
		}

		try {
			this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.dataFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveDataFile() {
		if(!this.dataFile.exists()) {
			createDataFile(this.fileName, this.path);
		}

		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, this.dataFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static Configuration createDataFile(String fileName, String path) {
		File dataFile = new File(BungeeMain.INSTANCE.getDataFolder() + path, fileName + ".yml");
		if(!dataFile.exists()) {
			try {
				boolean ignored = dataFile.getParentFile().mkdirs();
				boolean var4 = dataFile.createNewFile();
			} catch(IOException var5) {
				var5.printStackTrace();
			}
		}

		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void set(String path, Object object) {
		this.configuration.set(path, object);
	}

	public void addToList(String path, Object object) {
		List<?> list = this.getList(path) != null ? this.getList(path) : new ArrayList();
		((List) list).add(object);
		this.set(path, list);
	}

	public void removeFromList(String path, Object object) {
		List<?> list = this.getList(path);
		list.remove(object);
		this.set(path, list);
	}

	public Object get(String path) {
		return this.configuration.get(path);
	}

	public List<?> getList(String path) {
		return this.configuration.getList(path);
	}

	public String getString(String path) {
		return this.configuration.getString(path);
	}

	public boolean getBoolean(String path) {
		return this.configuration.getBoolean(path);
	}

	public int getInt(String path) {
		return this.configuration.getInt(path);
	}

	public double getDouble(String path) {
		return this.configuration.getDouble(path);
	}

	public List<String> getStringList(String path) {
		return this.configuration.getStringList(path);
	}
}
