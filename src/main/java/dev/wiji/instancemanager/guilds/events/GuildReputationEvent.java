package dev.wiji.instancemanager.guilds.events;

import dev.wiji.instancemanager.guilds.controllers.objects.Guild;

import java.util.ArrayList;
import java.util.List;

public class GuildReputationEvent extends GuildEvent {
	private int amount;
	private List<Double> multipliers = new ArrayList<>();

	public GuildReputationEvent(Guild guild, int amount) {
		super(guild);
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public void addMultiplier(double multiplier) {
		multipliers.add(multiplier);
	}

	public void clearMultipliers() {
		multipliers.clear();
	}

	public int getTotalReputation() {
		double amount = this.amount;
		for(Double multiplier : multipliers) amount *= multiplier;
		return (int) amount;
	}
}
