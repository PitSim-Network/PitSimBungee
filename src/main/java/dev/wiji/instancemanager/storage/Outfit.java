package dev.wiji.instancemanager.storage;

import dev.wiji.instancemanager.objects.PluginMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Outfit {
	private final int index;
	private String displayItemString;
	private final Map<String, String> itemStringLocationMap = new LinkedHashMap<>();

	public Outfit(int index) {
		this.index = index;
	}

	public void writeData(PluginMessage message) {
		message.writeInt(index)
				.writeString(displayItemString);

		message.writeInt(itemStringLocationMap.size());
		for(Map.Entry<String, String> entry : itemStringLocationMap.entrySet()) {
			message.writeString(entry.getKey());
			message.writeString(entry.getValue());
		}
	}

	public void updateData(PluginMessage message) {
		itemStringLocationMap.clear();

		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();

		displayItemString = strings.remove(0);
		int locationMapSize = integers.remove(0);
		for(int i = 0; i < locationMapSize; i++) {
			itemStringLocationMap.put(strings.remove(0), strings.remove(0));
		}
	}
}
