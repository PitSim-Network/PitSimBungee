package dev.wiji.instancemanager.objects;

public class OverworldServer extends PitSimServer {

	private static int NEXT_INDEX = 1;

	public OverworldServer(String pteroID) {
		super(pteroID, ServerType.OVERWORLD, NEXT_INDEX++);
	}
}
