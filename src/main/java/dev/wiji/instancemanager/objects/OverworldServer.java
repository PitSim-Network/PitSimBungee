package dev.wiji.instancemanager.objects;

public class OverworldServer extends MainGamemodeServer {

	private static int NEXT_INDEX = 1;

	public OverworldServer(String pteroID) {
		super(pteroID, ServerType.PITSIM, NEXT_INDEX++);
	}
}
