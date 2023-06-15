package net.pitsim.bungee.misc;

public abstract class ACommand extends ACommandBase {

	public ACommand(String executor) {
		super(executor);
	}

	public ACommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

}
