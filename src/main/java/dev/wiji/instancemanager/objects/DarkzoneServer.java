package dev.wiji.instancemanager.objects;

public class DarkzoneServer extends PitSimServer {

	private static int NEXT_INDEX = 1;

	public DarkzoneServer(String pteroID) {
		super(pteroID, ServerType.DARKZONE, NEXT_INDEX++);
	}
}