package dev.wiji.instancemanager.discord.commands;

import dev.wiji.instancemanager.aserverstatistics.StatisticCategory;
import dev.wiji.instancemanager.aserverstatistics.StatisticsManager;
import dev.wiji.instancemanager.discord.DiscordCommand;
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
//		Types of commands:
//		enchant usages (category)
//		combos usages (category, base enchant)
//		enchant usage vs time (category, enchant, timeframe)
//		combo usage vs time (category, enchant1, enchant2, timeframe)
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
					Map<String, Double> enchantUsageMap = new HashMap<>();
					if(finalFirstEnchant == null) {
						while(resultSet.next()) {
							String enchantRefName = resultSet.getString("enchant");
							int enchantHits;
							try {
								enchantHits = resultSet.getInt("total_hits");
								totalHits += enchantHits;
							} catch (SQLException e) {
								break;
							}
							enchantUsageMap.put(enchantRefName, enchantUsageMap.getOrDefault(enchantRefName, 0.0) + enchantHits);
						}
					} else {
						while(resultSet.next()) {
							String enchantRefName = resultSet.getString("enchant");
							if(!enchantRefName.equals(finalFirstEnchant.getRefName())) continue;

							totalHits = resultSet.getInt("total_hits");

							for(PitEnchant pitEnchant : PitEnchant.values()) {
								if(pitEnchant == finalFirstEnchant) continue;
								enchantUsageMap.put(pitEnchant.getRefName(), enchantUsageMap.getOrDefault(pitEnchant.getRefName(), 0.0) +
										resultSet.getInt(pitEnchant.getRefName()));
							}
						}
					}

					int finalTotalHits = totalHits;
					enchantUsageMap = enchantUsageMap.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.collect(Collectors.toMap(
									Map.Entry::getKey,
									entry -> entry.getValue() * 100.0 / finalTotalHits,
									(oldValue, newValue) -> oldValue, LinkedHashMap::new));

					List<Map.Entry<String, Double>> sortedEntries = enchantUsageMap.entrySet().stream()
							.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
							.limit(15)
							.collect(Collectors.toList());

					String labels = sortedEntries.stream()
							.map(entry -> "'" + PitEnchant.getEnchantByRefName(entry.getKey()).getRawName()
									.replaceAll("\"", "DOUBLEQUOTE").replaceAll("'", "SINGLEQUOTE") + "'")
							.collect(Collectors.joining(","));
					String data = sortedEntries.stream()
							.map(Map.Entry::getValue)
							.map(Object::toString)
							.collect(Collectors.joining(","));

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
					String formattedDate = LocalDate.now(ZoneId.of("America/New_York")).format(formatter);
					String title = "PitSim - " + formattedDate;

					String url = "https://quickchart.io/chart?width=600&height=350&v=2&&c={type:'bar',data:{labels:[" + labels +
							"],datasets:[{label:'',backgroundColor:'rgba(255, 170, 0, 0.5)'," +
							"borderColor:'rgb(255, 170, 0)',borderWidth:1.3,data:[" + data + "]}]},options:{title:" +
							"{display:true,text:'" + title + "',fontSize:20},legend:{display:false},scales: {yAxes: [{" +
							"scaleLabel: {display: true,labelString: 'Usage (%25)',fontColor: 'rgba(200, 200, 200, 0.5)',fontSize: 15,}," +
							"gridLines: {color: 'rgba(200, 200, 200, 0.5)',},ticks: " +
							"{beginAtZero: true,fontSize: 13,fontColor: 'rgba(200, 200, 200, 0.5)'," +
							"},},],xAxes: [{gridLines: {display: false},ticks: {fontSize: 13,fontColor: 'rgba(200, 200, 200, 0.5)',},},],},}}";
					url = url.replaceAll("'", "%27").replaceAll(" ", "%20")
							.replaceAll("DOUBLEQUOTE", "\"").replaceAll("SINGLEQUOTE", "\\\\'");

					event.reply(url).queue();
				} catch(SQLException e) {
					throw new RuntimeException(e);
				}
			});
		} else if(subCommand.equals("time")) {
//			TODO: Code
		}
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
}
