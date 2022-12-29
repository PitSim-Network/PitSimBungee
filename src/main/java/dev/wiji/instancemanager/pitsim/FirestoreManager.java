package dev.wiji.instancemanager.pitsim;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.misc.FileResourcesUtils;
import dev.wiji.instancemanager.objects.PlayerData;
import dev.wiji.instancemanager.ProxyRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FirestoreManager {
	public static Firestore FIRESTORE;

	public static ListenerRegistration registration;

	public static final String PLAYERDATA_COLLECTION = ConfigManager.isDev() ? "dev-playerdata" : "pitsim-playerdata";

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

		ApiFuture<QuerySnapshot> future = FIRESTORE.collection(PLAYERDATA_COLLECTION).get();
		List<QueryDocumentSnapshot> documents;
		try {
			documents = future.get().getDocuments();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		for(QueryDocumentSnapshot document : documents) {
			new PlayerData(UUID.fromString(document.getId()), document);
		}


		registration = FirestoreManager.FIRESTORE.collection(FirestoreManager.PLAYERDATA_COLLECTION)

				.addSnapshotListener(
						(snapshots, exception) -> {
							if(exception != null) {
								exception.printStackTrace();
								return;
							}

							for(DocumentChange modifiedDocument : snapshots.getDocumentChanges()) {
								DocumentSnapshot playerData = modifiedDocument.getDocument();

								new PlayerData(UUID.fromString(playerData.getId()), playerData);
							}
						});


		//run the leaderboard calc init after 5 secondsc
		((ProxyRunnable) LeaderboardCalc::init).runAfterEvery(30, 30, TimeUnit.SECONDS);
	}

}
