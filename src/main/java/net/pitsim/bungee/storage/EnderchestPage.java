package net.pitsim.bungee.storage;

import net.pitsim.bungee.objects.PluginMessage;

import java.util.List;

public class EnderchestPage {
	private final int index;
	private String displayItemString;
	private boolean isWardrobeEnabled;
	private final String[] itemStrings = new String[36];

	public EnderchestPage(int index) {
		this.index = index;
	}

	public void writeData(PluginMessage message) {
		message.writeInt(index)
				.writeString(displayItemString)
				.writeBoolean(isWardrobeEnabled);
		for(String item : itemStrings) message.writeString(item);
	}

	public void updateData(PluginMessage message) {
		List<String> strings = message.getStrings();
		List<Boolean> booleans = message.getBooleans();

		displayItemString = strings.remove(0);
		isWardrobeEnabled = booleans.remove(0);
		for(int i = 0; i < itemStrings.length; i++) itemStrings[i] = strings.remove(0);
	}

	public int getIndex() {
		return index;
	}

	public String getDisplayItemString() {
		return displayItemString;
	}

	public boolean isWardrobeEnabled() {
		return isWardrobeEnabled;
	}

	public String[] getItemStrings() {
		return itemStrings;
	}
}
