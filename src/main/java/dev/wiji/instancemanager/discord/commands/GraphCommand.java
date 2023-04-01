package dev.wiji.instancemanager.discord.commands;

import dev.wiji.instancemanager.aserverstatistics.StatisticCategory;
import dev.wiji.instancemanager.aserverstatistics.StatisticsManager;
import dev.wiji.instancemanager.discord.DiscordCommand;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.pitsim.PitEnchant;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GraphCommand extends DiscordCommand {
	public GraphCommand() {
		super("graph");
		requireVerification = true;
	}

	@Override
	public SlashCommandData getCommandStructure() {
		OptionData categoryOption = new OptionData(OptionType.STRING, "category", "the category of data to query", true);
		for(StatisticCategory category : StatisticCategory.values()) categoryOption.addChoice(category.getDisplayName().toLowerCase(), category.name());

		return Commands.slash(name, "create fun graphs based on data from pitsim").addSubcommands(
				new SubcommandData("popularity", "graph popular enchants or enchant combinations")
						.addOptions(
								categoryOption,
								new OptionData(OptionType.STRING, "enchant", "the enchant to look up", false, true)
						),
				new SubcommandData("time", "graph an enchant or enchant combination against time")
						.addOptions(
								categoryOption,
								new OptionData(OptionType.STRING, "timeframe", "the time frame to query from", true)
										.addChoice("last week", "days")
										.addChoice("last month", "weeks"),
								new OptionData(OptionType.STRING, "enchant", "the enchant to look up", true, true),
								new OptionData(OptionType.STRING, "second-enchant", "combination with the first enchant", false, true)
						)
		);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		String subCommand = event.getSubcommandName();
		if(subCommand == null) {
			event.reply("Please run a sub command").setEphemeral(true).queue();
			return;
		}

		StatisticCategory category = StatisticCategory.valueOf(event.getOption("category").getAsString());
		PitEnchant firstEnchant = null;
		if(event.getOption("enchant") != null) {
			firstEnchant = PitEnchant.getEnchantBySimpleName(event.getOption("enchant").getAsString());
			if(firstEnchant == null) {
				event.reply("Invalid enchant").setEphemeral(true).queue();
				return;
			}
		}
		PitEnchant secondEnchant = null;
		if(event.getOption("second-enchant") != null) {
			secondEnchant = PitEnchant.getEnchantBySimpleName(event.getOption("second-enchant").getAsString());
			if(secondEnchant == null) {
				event.reply("Invalid second enchant").setEphemeral(true).queue();
				return;
			}
		}

		PitEnchant finalFirstEnchant = firstEnchant;
		PitEnchant finalSecondEnchant = secondEnchant;
		if(subCommand.equals("popularity")) {
			StatisticsManager.queryByCategory(category, resultSet -> {
				try {
					int totalHits = 0;
					Map<String, Double> usageMap = new LinkedHashMap<>();
					if(finalFirstEnchant == null) {
						while(resultSet.next()) {
							long date = resultSet.getLong("date");
							double multiplier = getMultiplier(date);
							if(multiplier == 0) continue;

							String enchantRefName = resultSet.getString("enchant");
							int enchantHits;
							try {
								enchantHits = resultSet.getInt("total_hits");
								enchantHits *= multiplier;
							} catch (SQLException e) {
								break;
							}
							if(enchantRefName == null) {
								totalHits += enchantHits;
								continue;
							}
							usageMap.put(enchantRefName, usageMap.getOrDefault(enchantRefName, 0.0) + enchantHits);
						}
					} else {
						while(resultSet.next()) {
							long date = resultSet.getLong("date");
							double multiplier = getMultiplier(date);
							if(multiplier == 0) continue;

							String enchantRefName = resultSet.getString("enchant");
							if(enchantRefName == null) {
								totalHits += resultSet.getInt("total_hits") * multiplier;
								continue;
							}
							if(!enchantRefName.equals(finalFirstEnchant.getRefName())) continue;

							for(PitEnchant pitEnchant : PitEnchant.values()) {
								if(pitEnchant == finalFirstEnchant) continue;
								usageMap.put(pitEnchant.getRefName(), usageMap.getOrDefault(pitEnchant.getRefName(), 0.0) +
										resultSet.getInt(pitEnchant.getRefName()) * multiplier);
							}
						}
					}

					int finalTotalHits = totalHits;
					usageMap = usageMap.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.collect(Collectors.toMap(
									Map.Entry::getKey,
									entry -> entry.getValue() * 100.0 / finalTotalHits,
									(oldValue, newValue) -> oldValue, LinkedHashMap::new));

					List<Map.Entry<String, Double>> sortedEntries = usageMap.entrySet().stream()
							.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
							.limit(15)
							.collect(Collectors.toList());

					String labels = sortedEntries.stream()
							.map(entry -> "'" + encodeString(PitEnchant.getEnchantByRefName(entry.getKey()).getShortenedRawName()) + "'")
							.collect(Collectors.joining(","));
					String data = sortedEntries.stream()
							.map(Map.Entry::getValue)
							.map(Object::toString)
							.collect(Collectors.joining(","));

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
					String formattedDate = LocalDate.now(ZoneId.of("America/New_York")).format(formatter);
					String title = "Popular Enchants";
					if(finalFirstEnchant != null) title += " w/" + finalFirstEnchant.getShortenedRawName();
					title += " - PitSim " + formattedDate;

					String chart = createChart(title, null, "Usage (%)", labels, data, true);
					event.reply(chart).queue();
				} catch(SQLException e) {
					throw new RuntimeException(e);
				}
			});
		} else if(subCommand.equals("time")) {
			TimeFrame timeFrame = TimeFrame.getTimeFrame(event.getOption("timeframe").getAsString());
			assert timeFrame != null;
			StatisticsManager.queryByCategory(category, resultSet -> {
				try {
//					Usage map contains int usage num for enchant/combo, total contains the total
//					Usage map is then divided by total usage map to store usage %
					Map<Integer, Double> usageMap = new LinkedHashMap<>();
					Map<Integer, Integer> totalUsageMap = new LinkedHashMap<>();
					for(int i = 0; i < timeFrame.segmentCount; i++) {
						usageMap.put(i + 1, 0.0);
						totalUsageMap.put(i + 1, 0);
					}
					while(resultSet.next()) {
						String enchantRefName = resultSet.getString("enchant");
						boolean isDefault = enchantRefName == null;
						boolean isRequested = enchantRefName != null && enchantRefName.equals(finalFirstEnchant.getRefName());
						if(!isDefault && !isRequested) continue;

						int enchantHits = resultSet.getInt("total_hits");
						long date = resultSet.getLong("date");
						int segmentNum = timeFrame.getUnitsOfTimeAgo(date);
						if(segmentNum > timeFrame.segmentCount) continue;

						if(isDefault) {
							totalUsageMap.put(segmentNum, totalUsageMap.getOrDefault(segmentNum, 0) + enchantHits);
						} else { // if isRequested
							usageMap.put(segmentNum, usageMap.getOrDefault(segmentNum, 0.0) +
									(finalSecondEnchant == null ? enchantHits : resultSet.getInt(finalSecondEnchant.getRefName())));
						}
					}

					usageMap = usageMap.entrySet().stream().collect(Collectors.toMap(
							Map.Entry::getKey,
							entry -> entry.getValue() * 100.0 / totalUsageMap.get(entry.getKey()),
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));

					String labels = String.join(",", Collections.nCopies(usageMap.size(), "''"));
					String data = usageMap.values().stream()
							.map(Object::toString)
							.collect(Collectors.joining(","));

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
					String formattedDate = LocalDate.now(ZoneId.of("America/New_York")).format(formatter);
					String title = finalFirstEnchant.getShortenedRawName();
					if(finalSecondEnchant != null) title += " & " + finalSecondEnchant.getShortenedRawName();
					title += " vs Time - PitSim " + formattedDate;

					String chart = createChart(title, "Time (" + timeFrame.getPluralDisplayName() + ")",
							"Usage (%)", labels, data, true);
					event.reply(chart).queue();
				} catch(SQLException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	public static String createChart(String title, String xAxisTitle, String yAxisTitle, String labels, String data, boolean showLabels) {
		title = encodeString(title);
		xAxisTitle = encodeString(xAxisTitle);
		yAxisTitle = encodeString(yAxisTitle);
		boolean showXAxis = xAxisTitle != null;
		boolean showYAxis = yAxisTitle != null;
		String chart = "https://quickchart.io/chart?width=600&height=350&v=2&&c={type:'bar',data:{labels:[" + labels +
				"],datasets:[{label:'',backgroundColor:'rgba(255, 170, 0, 0.5)'," +
				"borderColor:'rgb(255, 170, 0)',borderWidth:1.3,data:[" + data + "]}]}," +
				"options:{" +
				"title:{display:true,text:'" + title + "',fontSize:18}," +
				"legend:{display:false}," +
				"scales: {" +
				"yAxes: [{" +
					"scaleLabel: {display: " + showYAxis + ",labelString: '" + yAxisTitle + "',fontColor: 'rgba(200, 200, 200, 0.5)',fontSize: 15,}," +
					"gridLines: {color: 'rgba(200, 200, 200, 0.5)',tickMarkLength: 0,}," +
					"ticks: {beginAtZero: true,fontSize: 13,fontColor: 'rgba(200, 200, 200, 0.5)',padding: 10,}," +
				"},]," +
				"xAxes: [{" +
					"scaleLabel: {display: " + showXAxis + ",labelString: '" + xAxisTitle + "',fontColor: 'rgba(200, 200, 200, 0.5)',fontSize: 15,}," +
					"gridLines: {display: false}," +
					"ticks: {fontSize: 13,display:" + showLabels + ",fontColor: 'rgba(200, 200, 200, 0.5)',}," +
				"},],},}}";
		return chart.replaceAll("'", "%27").replaceAll(" ", "%20")
				.replaceAll("DOUBLEQUOTE", "\"").replaceAll("SINGLEQUOTE", "\\\\'");
	}

	public static String encodeString(String string) {
		if(string == null) return null;
		return string.replaceAll("\"", "DOUBLEQUOTE").replaceAll("'", "SINGLEQUOTE")
				.replaceAll("%", "%25").replaceAll("&", "%26");
	}

	public static double getMultiplier(long date) {
		double daysPassed = (System.currentTimeMillis() - date) / (1000.0 * 60 * 60 * 24);
		return Math.max(Math.min(1 - (daysPassed - 7) / 7.0, 1), 0);
	}

	@Override
	public List<Command.Choice> autoComplete(CommandAutoCompleteInteractionEvent event, String currentOption, String currentValue) {
		List<PitEnchant> pitEnchants = new ArrayList<>();
		String subCommand = event.getSubcommandName();

		if(currentOption.contains("enchant")) {
			for(PitEnchant pitEnchant : PitEnchant.values()) {
				if(currentValue.isEmpty()) {
					pitEnchants.add(pitEnchant);
				} else if(pitEnchant.getSimpleName().toLowerCase().startsWith(currentValue)) pitEnchants.add(pitEnchant);
			}
			for(PitEnchant pitEnchant : PitEnchant.values()) {
				if(pitEnchants.contains(pitEnchant)) continue;
				String[] words = pitEnchant.getSimpleName().toLowerCase().split(" ");
				for(String word : words) {
					if(!word.startsWith(currentValue)) continue;
					pitEnchants.add(pitEnchant);
					break;
				}
			}
		}

		List<Command.Choice> choices = new ArrayList<>();
		for(PitEnchant pitEnchant : pitEnchants) {
			String enchantName = pitEnchant.getSimpleName().toLowerCase();
			Command.Choice choice = new Command.Choice(enchantName, enchantName);
			choices.add(choice);
		}
		return choices;
	}

	public enum TimeFrame {
		DAYS("Day", "days", 7, 1000L * 60 * 60 * 24),
		WEEKS("Week", "weeks", 4, 1000L * 60 * 60 * 24 * 7);

		private final String displayName;
		private final String refName;
		private final int segmentCount;
		private final long duration;

		TimeFrame(String displayName, String refName, int segmentCount, long duration) {
			this.displayName = displayName;
			this.refName = refName;
			this.segmentCount = segmentCount;
			this.duration = duration;
		}

		public int getUnitsOfTimeAgo(long date) {
			return (int) ((System.currentTimeMillis() - date - 1) / duration + 1);
		}

		public String getDisplayName(int amount) {
			return displayName + Misc.s(amount);
		}

		public String getSingularDisplayName() {
			return displayName;
		}

		public String getPluralDisplayName() {
			return displayName + "s";
		}

		public String getRefName() {
			return refName;
		}

		public static TimeFrame getTimeFrame(String refName) {
			for(TimeFrame timeFrame : values()) if(timeFrame.getRefName().equals(refName)) return timeFrame;
			return null;
		}
	}
}
