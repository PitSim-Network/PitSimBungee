package net.pitsim.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface ProxyRunnable extends Runnable {
	default ScheduledTask runAsync() {
		return ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.INSTANCE, this);
	}

	default ScheduledTask runAfter(long delay, TimeUnit unit) {
		return ProxyServer.getInstance().getScheduler().schedule(BungeeMain.INSTANCE, this, delay, unit);
	}

	default ScheduledTask runAfterEvery(long delay, long repeat, TimeUnit unit) {
		return ProxyServer.getInstance().getScheduler().schedule(BungeeMain.INSTANCE, this, delay, repeat, unit);
	}

	static void cancel(ProxyRunnable task) {
		ProxyServer.getInstance().getScheduler().cancel((ScheduledTask) task);
	}
}
