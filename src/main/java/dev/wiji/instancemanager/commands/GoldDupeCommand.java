package dev.wiji.instancemanager.commands;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import dev.wiji.instancemanager.objects.PlayerData;
import dev.wiji.instancemanager.pitsim.FirestoreManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GoldDupeCommand extends Command {

	Firestore FIRESTORE = FirestoreManager.FIRESTORE;

	public Map<UUID, Double> goldMap = new HashMap<>();

	public GoldDupeCommand() {
		super("golddupe");
	}


	@Override
	public void execute(CommandSender sender, String[] args) {
		getGoldCount();
	}

	public void getGoldCount() {

	ApiFuture<QuerySnapshot> future = FIRESTORE.collection("pitsim-playerdata").get();
	List<QueryDocumentSnapshot> documents;
		try

	{
		documents = future.get().getDocuments();
	} catch(InterruptedException |
	ExecutionException e)

	{
		throw new RuntimeException(e);
	}
		for(QueryDocumentSnapshot document :documents) {
			UUID id = UUID.fromString(document.getId());
			double gold = document.getDouble("gold");
			if(gold > 100000000) System.out.println(id.toString() + ": " + gold);
	}
	}


}
