import java.util.Collections;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		int NEW_SERVER_THRESHOLD = 8;
		int REQUIRED_DROP_FOR_SHUTDOWN = 4;
		List<Integer> serverList = Collections.nCopies(5, 0);

		for(int j = 0; j < 22; j++) {
			int players = j;
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println(j);
			for(int i = 0; i < Math.min(players / NEW_SERVER_THRESHOLD + 1, serverList.size()); i++) {
				System.out.println("Turning on server: " + (i + 1));
			}

			for(int i = 1 + (players + REQUIRED_DROP_FOR_SHUTDOWN - 1) / NEW_SERVER_THRESHOLD; i < serverList.size(); i++) {
				System.out.println("Shutting down server: " + (i + 1));
			}
		}
	}
}
