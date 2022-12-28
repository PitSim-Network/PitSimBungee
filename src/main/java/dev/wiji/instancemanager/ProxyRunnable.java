package dev.wiji.instancemanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface ProxyRunnable extends Runnable {
	default ScheduledTask runAsync() {
		return ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.INSTANCE, this);
	}

	default ScheduledTask runAfter(long time, TimeUnit unit) {
		return ProxyServer.getInstance().getScheduler().schedule(BungeeMain.INSTANCE, this, time, unit);
	}

	default ScheduledTask runAfterEvery(long time, long repeat, TimeUnit unit) {
		return ProxyServer.getInstance().getScheduler().schedule(BungeeMain.INSTANCE, this, time, repeat, unit);
	}

	default void cancel(ProxyRunnable task) {
		ProxyServer.getInstance().getScheduler().cancel((ScheduledTask) task);
	}
}
