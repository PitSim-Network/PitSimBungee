import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {
//	The next server turns on when the player count reaches a multiple of this number
	public static final int NEW_SERVER_THRESHOLD = 10;
//	When the player count drops this many below a multiple of the number above, that server enabled by hitting
//	that threshold is no longer needed and gets shut down
	public static final int REQUIRED_DROP_FOR_SHUTDOWN = 4;

	public static void main(String[] args) {
		int players = 16;
		List<Integer> serverList = new ArrayList<>();
		serverList.addAll(Collections.nCopies(5, 0));

		for(int i = 0; i < Math.min(players / NEW_SERVER_THRESHOLD + 1, serverList.size()); i++) {
			System.out.println("Turning on server: " + (i + 1));
		}

		for(int i = 1 + (players + REQUIRED_DROP_FOR_SHUTDOWN - 1) / NEW_SERVER_THRESHOLD; i < serverList.size(); i++) {
			System.out.println("Shutting down server: " + (i + 1));
		}
	}
}
