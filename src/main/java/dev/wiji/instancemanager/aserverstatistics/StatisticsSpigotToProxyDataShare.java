package dev.wiji.instancemanager.aserverstatistics;

import dev.wiji.instancemanager.events.MessageEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsSpigotToProxyDataShare {
	public Map<String, String> enchantInfoMap = new LinkedHashMap<>();

	public StatisticsSpigotToProxyDataShare(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		List<Integer> integers = event.getMessage().getIntegers();
		List<Long> longs = event.getMessage().getLongs();

		int enchants = integers.remove(0);
		for(int i = 0; i < enchants; i++) {
			String enchantName = strings.remove(0);
			String enchantRefName = strings.remove(0);
			enchantInfoMap.put(enchantRefName, enchantName);
		}
	}

	public String getEnchantName(String refName) {
		return enchantInfoMap.get(refName);
	}
}
