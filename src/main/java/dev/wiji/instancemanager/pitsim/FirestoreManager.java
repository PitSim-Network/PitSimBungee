package dev.wiji.instancemanager.pitsim;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.core.ApiFuture;
import com.google.api.services.storage.StorageScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.mattmalec.pterodactyl4j.client.entities.Backup;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.FileResourcesUtils;
import dev.wiji.instancemanager.objects.PlayerData;
import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FirestoreManager {
	public static Firestore FIRESTORE;

	public static ListenerRegistration registration;
	private static GoogleCredentials credentials;
	private static String accessToken;

	public static final String PLAYERDATA_COLLECTION = ConfigManager.isDev() ? "dev-playerdata" : "pitsim-playerdata";

	public static void init() {

		try {
			System.out.println("Loading PitSim database");
			InputStream serviceAccount = new FileResourcesUtils().getFileFromResourceAsStream("google-key.json");
			credentials = GoogleCredentials.fromStream(serviceAccount);

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


		//run the leaderboard calc init after 5 seconds, then every 30 minutes
		((ProxyRunnable) LeaderboardCalc::init).runAfterEvery(30, 60 * 30, TimeUnit.SECONDS);
	}

	private static final String FIRESTORE_PROJECT_ID = "pitsim-network";
	private static final String FIRESTORE_API_ENDPOINT = "https://firestore.googleapis.com/v1/projects/" + FIRESTORE_PROJECT_ID + "/databases/(default):";
	private static final String AUTOMATIC_BUCKET_PATH = "gs://pitsim-automatic-backups";
	private static final String MANUAL_BUCKET_PATH = "gs://pitsim-backups";

	public static void takeBackup(boolean manual) throws IOException {
		GoogleCredentials updatedCredentials = credentials.createScoped(StorageScopes.DEVSTORAGE_FULL_CONTROL, StorageScopes.CLOUD_PLATFORM);
		accessToken = updatedCredentials.refreshAccessToken().getTokenValue();

		String exportUri = FIRESTORE_API_ENDPOINT + "exportDocuments?name=projects/" + FIRESTORE_PROJECT_ID + "/databases/(default)";
		String requestBody = "{\n" +
				"  \"collectionIds\": [\n" +
				"    \"" + PLAYERDATA_COLLECTION + "\"\n" +
				"  ],\n" +
				"  \"outputUriPrefix\": \"" + (manual ? MANUAL_BUCKET_PATH : AUTOMATIC_BUCKET_PATH) + "\"\n" +
				"}";
		String exportName = sendPostRequest(exportUri, requestBody);

		AOutput.log("FIRESTORE BACKUP TAKEN");
		AOutput.log("----------------------------------");
		AOutput.log(exportName);
		AOutput.log("----------------------------------");
	}

	public static void takeItemBackup() {
		ClientServer server = BungeeMain.client.retrieveServerByIdentifier(ConfigManager.getProxyServer()).execute();
		int backupLimit = Integer.parseInt(server.getFeatureLimits().getBackups());
		List<Backup> backups = server.retrieveBackups().execute();

		Backup oldestBackup = null;
		if(backups.size() >= backupLimit && backupLimit != 0) {
			for(Backup backup : backups) {
				if(backup.isLocked()) continue;
				if(oldestBackup == null) oldestBackup = backup;

				if(backup.getTimeCreated().isBefore(oldestBackup.getTimeCreated())) {
					oldestBackup = backup;
				}
			}
			server.getBackupManager().deleteBackup(oldestBackup).execute();
		}

		server.getBackupManager().createBackup().execute();
	}

	private static String sendPostRequest(String uri, String requestBody) throws IOException {

		HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(credentials);
		HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(adapter);
		HttpRequest httpRequest = requestFactory.buildPostRequest(new GenericUrl(uri), ByteArrayContent.fromString("application/json", requestBody));

		com.google.api.client.http.HttpHeaders headers = new com.google.api.client.http.HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
		httpRequest.setHeaders(headers);

		com.google.api.client.http.HttpResponse httpResponse = httpRequest.execute();

		return httpResponse.parseAsString();
	}
}




