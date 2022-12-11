package dev.wiji.instancemanager.storage;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.plugin.Listener;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DupeManager implements Listener {

	static {
		new Thread(() -> {
			try {
				MojangsonParser.parse("{}");
			} catch(Exception exception) {
				exception.printStackTrace();
			}
			File folder = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/");
			File[] fileArray = folder.listFiles();

			for(int i = 0; i < Objects.requireNonNull(fileArray).length; i++) {
				if(!fileArray[i].isFile()) continue;
				StorageProfile storageProfile = loadPlayer(fileArray[i].getName());

				List<String> itemStrings = new ArrayList<>();
				itemStrings.addAll(Arrays.asList(storageProfile.getInventoryStrings()));
				itemStrings.addAll(Arrays.asList(storageProfile.getArmor()));
				for(String[] strings : storageProfile.getEnderchest()) itemStrings.addAll(Arrays.asList(strings));

				for(String itemString : itemStrings) {
					if(itemString == null || itemString.isEmpty()) continue;
//					System.out.println(fileArray[i].getName());
//					System.out.println("string: " + itemString);

					ItemSerialize.LimitedItemStack itemStack = ItemSerialize.deserialize(itemString);
//					System.out.println("nbt: " + itemStack.nbtData.getString("pr-uuid"));
				}
			}
		}).start();
	}

	public static StorageProfile loadPlayer(String fileName) {
		try {
			File file = new File(BungeeMain.INSTANCE.getDataFolder() + "/itemstorage/" + fileName);
			Reader reader = Files.newBufferedReader(file.toPath());
			return new Gson().fromJson(reader, StorageProfile.class);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
}

class ItemSerialize {
//	public static String serialize(ItemStack i) {
//		String[] parts = new String[7];
//		parts[0] = i.getType().name();
//		parts[1] = Integer.toString(i.getAmount());
//		parts[2] = String.valueOf(i.getDurability());
//		parts[3] = i.getItemMeta().getDisplayName();
//		parts[4] = String.valueOf(i.getData().getData());
//		parts[5] = getEnchants(i);
//		parts[6] = getNBT(i);
//		return StringUtils.join(parts, ";");
//	}

	public static LimitedItemStack deserialize(String p) {
		String[] a = p.split(";");
		return new LimitedItemStack(a[0], a[1], a[2], a[3], a[6]);
	}

	public static class LimitedItemStack {
		public Material material;
		public int amount;
		public short data;
		public String displayName;
		public NBTTagCompound nbtData;

		public LimitedItemStack(String material, String amount, String data, String displayName, String nbtData) {
			this.material = Material.getMaterial(material);
			this.amount = Integer.parseInt(amount);
			this.data = Short.parseShort(data);
			this.displayName = displayName;
			try {
				this.nbtData = MojangsonParser.parse(nbtData);
			} catch(MojangsonParseException exception) {
				throw new RuntimeException(exception);
			}
		}
	}
}