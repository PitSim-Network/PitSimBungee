package dev.wiji.instancemanager.misc;

public abstract class ACommand extends ACommandBase {

	public ACommand(String executor) {
		super(executor);
	}

	public ACommand(AMultiCommand base, String executor) {
		super(base, executor);
	}

}
