package dev.wiji.instancemanager.aserverstatistics;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.pitsim.PitEnchant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticDataChunk {
	private final long startTime;
	public List<Record> records = new ArrayList<>();

	public StatisticDataChunk(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		List<Integer> integers = event.getMessage().getIntegers();
		List<Long> longs = event.getMessage().getLongs();

		this.startTime = longs.remove(0);
		int recordCount = integers.remove(0);

		for(int i = 0; i < recordCount; i++) {
			String enchantName = strings.remove(0);
			if(enchantName.isEmpty()) enchantName = null;
			StatisticCategory category = StatisticCategory.valueOf(strings.remove(0));
			int totalHits = integers.remove(0);
			LinkedHashMap<String, Integer> hitsWithEnchant = new LinkedHashMap<>();
			for(String enchantRefName : PitEnchant.getAllRefNames())
				hitsWithEnchant.put(enchantRefName, integers.remove(0));

			Record record = new Record(enchantName, category, totalHits, hitsWithEnchant);
			records.add(record);
		}
	}

	public void print() {
		AOutput.log("------------------------------");
		AOutput.log("Start Time: " + startTime);
		AOutput.log("Records: " + records.size());
		for(Record record : records) {
			AOutput.log("Enchant: " + record.enchantRefName + ", Category: " + record.category + ", Hits: " + record.totalHits);
//			for(Map.Entry<String, Integer> entry : record.hitsWithEnchant.entrySet()) {
//				String enchantRefName = entry.getKey();
//				int hits = entry.getValue();
//				if(hits == 0) continue;
//				if(!record.enchantRefName.equals(enchantRefName)) {
//					AOutput.log("Combination of " + record.enchantRefName + " with " + enchantRefName + ": " + hits);
//				}
//			}
		}
		AOutput.log("------------------------------");
	}

	public long getStartTime() {
		return startTime;
	}

	public static class Record {
		private final String enchantRefName;
		private final StatisticCategory category;
		private final int totalHits;
		private final LinkedHashMap<String, Integer> hitsWithEnchant;

		public Record(String enchantRefName, StatisticCategory category, int totalHits, LinkedHashMap<String, Integer> hitsWithEnchant) {
			this.enchantRefName = enchantRefName;
			this.category = category;
			this.totalHits = totalHits;
			this.hitsWithEnchant = hitsWithEnchant;
		}

		public String getEnchantRefName() {
			return enchantRefName;
		}

		public StatisticCategory getCategory() {
			return category;
		}

		public int getTotalHits() {
			return totalHits;
		}

		public Map<String, Integer> getHitsWithEnchant() {
			return hitsWithEnchant;
		}
	}
}
