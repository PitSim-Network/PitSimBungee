package net.pitsim.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	public static List<Long> sqlThreadIDs = new ArrayList<>();

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if(!player.hasPermission("pitsim.admin")) return;

			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			ThreadInfo[] infos = bean.dumpAllThreads(true, true);
//			System.out.println(Arrays.stream(infos).map(Object::toString)
//					.collect(Collectors.joining()));


			if(args.length > 0 && args[0].equalsIgnoreCase("dump")) {

				for (ThreadInfo info : infos) {
					if(!info.getThreadName().contains("PitSim")) continue;
					System.out.println(info.toString());
				}

				return;
			}




			int overlap = 0;
			int pitThreads = 0;

			for (ThreadInfo info : infos) {
//				if(!info.id().contains("PitSim")) continue;
//				System.out.println(info.toString());
				if(info.getThreadName().contains("PitSim")) pitThreads++;

				if (sqlThreadIDs.contains(info.getThreadId())) {
					overlap++;
					System.out.println(info.getThreadState());
				}
			}

			System.out.println("SQL Overlap: " + overlap);
			System.out.println("Pit Threads: " + pitThreads);


//			AOutput.log("There are " + TableManager.openResultSets + "/" + TableManager.totalResultSets + " result sets open");
		}
	}
}
