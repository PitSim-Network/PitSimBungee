package dev.wiji.instancemanager.PitSim;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Misc.FileResourcesUtils;
import dev.wiji.instancemanager.Objects.Leaderboard;
import dev.wiji.instancemanager.Objects.PlayerData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FirestoreManager {
	public static Firestore FIRESTORE;

	public static ListenerRegistration registration;
	public static ListenerRegistration initialRegistration;

	public static final String PLAYERDATA_COLLECTION = "pitsim-playerdata";

	public static void init() {

		try {
			System.out.println("Loading PitSim database");
			InputStream serviceAccount = new FileResourcesUtils().getFileFromResourceAsStream("google-key.json");
			GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(credentials)
					.build();
			try {
				FirebaseApp.initializeApp(options);
			} catch(IllegalStateException exception) {
				System.out.println("Firestore already initialized");
			}

			FIRESTORE = FirestoreClient.getFirestore();
			System.out.println("PitSim database loaded!");
		} catch(IOException exception) {
			System.out.println("PitSim database failed to load. Disabling plugin...");
			BungeeMain.INSTANCE.onDisable();
			return;
		}

		initialRegistration = FirestoreManager.FIRESTORE.collection(FirestoreManager.PLAYERDATA_COLLECTION)

				.addSnapshotListener(
						(snapshots, exception) -> {
							if(exception != null) {
								exception.printStackTrace();
								return;
							}

							for(DocumentSnapshot document : snapshots.getDocuments()) {

								System.out.println(document.getId());

								new PlayerData(UUID.fromString(document.getId()), document);
							}
						});


		registration = FirestoreManager.FIRESTORE.collection(FirestoreManager.PLAYERDATA_COLLECTION)

				.addSnapshotListener(
						(snapshots, exception) -> {
							if(exception != null) {
								exception.printStackTrace();
								return;
							}

							for(DocumentChange modifiedDocument : snapshots.getDocumentChanges()) {
								DocumentSnapshot playerData = modifiedDocument.getDocument();

								System.out.println(playerData.getId());

								new PlayerData(UUID.fromString(playerData.getId()), playerData);
							}
						});



		LeaderboardCalc.init();
	}

}
