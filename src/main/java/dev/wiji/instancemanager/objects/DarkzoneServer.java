package dev.wiji.instancemanager.objects;

public class DarkzoneServer extends MainGamemodeServer {

	private static int NEXT_INDEX = 1;

	public DarkzoneServer(String pteroID) {
		super(pteroID, ServerType.DARKZONE, NEXT_INDEX++);
	}
}