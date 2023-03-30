package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.aserverstatistics.StatisticCategory;
import dev.wiji.instancemanager.aserverstatistics.StatisticsManager;
import dev.wiji.instancemanager.discord.Constants;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class KyroCommand extends Command {
	public KyroCommand(Plugin bungeeMain) {
		super("kyro");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(!Misc.isKyro(player.getUniqueId())) return;

		StatisticsManager.queryByCategory(StatisticCategory.OVERWORLD_STREAKING, resultSet -> {
			try {
				int totalHits = 0;
				Map<String, Double> enchantUsageMap = new HashMap<>();
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

				int finalTotalHits = totalHits;
				enchantUsageMap = enchantUsageMap.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue() * 100.0 / finalTotalHits,
								(oldValue, newValue) -> oldValue, LinkedHashMap::new));

				List<Map.Entry<String, Double>> sortedEntries = enchantUsageMap.entrySet().stream()
						.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
						.limit(10)
						.collect(Collectors.toList());

				String labels = sortedEntries.stream()
						.map(entry -> "'" + StatisticsManager.sharedData.getEnchantName(entry.getKey())
								.replaceAll("\"", "").replaceAll("'", "") + "'")
						.collect(Collectors.joining(","));
				String data = sortedEntries.stream()
						.map(Map.Entry::getValue)
						.map(Object::toString)
						.collect(Collectors.joining(","));

				String url = "https://quickchart.io/chart?c={type:'bar',data:{labels:[" + labels +
						"],datasets:[{label:'Enchant Usage Percent',data:[" + data + "]}]}}";
				url = url.replaceAll("'", "%27").replaceAll(" ", "%20");

				System.out.println(url);
				TextChannel textChannel = DiscordManager.MAIN_GUILD.getTextChannelById(Constants.STAFF_GENERAL_CHANNEL);
				assert textChannel != null;
				textChannel.sendMessage(url).queue();
				AOutput.color(player, "&6&lCHART!&7 Sent chart to discord");
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
