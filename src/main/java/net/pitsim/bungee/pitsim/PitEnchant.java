package net.pitsim.bungee.pitsim;

import java.util.ArrayList;
import java.util.List;

public enum PitEnchant {
//	COMBO_VENOM("&dRARE! &9Combo: Venom", "Combo: Venom", "venom", true, true, false),
	SELFCHECKOUT("&dRARE! &9Self-Checkout", "Self-Checkout", "selfcheckout", true, true, false),
	BILLIONAIRE("&dRARE! &9Billionaire", "Billionaire", "bill", true, true, false),
	COMBO_PERUNS_WRATH("&dRARE! &9Combo: Perun's Wrath", "Combo: Perun's Wrath", "perun", true, true, false),
	EXECUTIONER("&dRARE! &9Executioner", "Executioner", "executioner", true, true, false),
	GAMBLE("&dRARE! &9Gamble", "Gamble", "gamble", true, true, false),
	COMBO_STUN("&dRARE! &9Combo: Stun", "Combo: Stun", "combostun", true, true, false),
	SPEEDY_HIT("&dRARE! &9Speedy Hit", "Speedy Hit", "speedyhit", true, true, false),
	HEALER("&dRARE! &9Healer", "Healer", "healer", true, true, false),
	LIFESTEAL("&9Lifesteal", "Lifesteal", "ls", false, true, false),
	COMBO_HEAL("&9Combo: Heal", "Combo: Heal", "comboheal", false, true, false),
	SHARK("&9Shark", "Shark", "shark", false, false, false),
	PAIN_FOCUS("&9Pain Focus", "Pain Focus", "painfocus", false, true, false),
	DIAMOND_STOMP("&9Diamond Stomp", "Diamond Stomp", "diamondstomp", false, true, false),
	COMBO_DAMAGE("&9Combo: Damage", "Combo: Damage", "combodamage", false, false, false),
	BERSERKER("&9Berserker", "Berserker", "berserker", false, true, false),
	KING_BUSTER("&9King Buster", "King Buster", "kb", false, false, false),
	SHARP("&9Sharp", "Sharp", "sharp", false, false, false),
	PUNISHER("&9Punisher", "Punisher", "pun", false, false, false),
	BEAT_THE_SPAMMERS("&9Beat the Spammers", "Beat the Spammers", "bts", false, false, false),
	GOLD_AND_BOOSTED("&9Gold and Boosted", "Gold and Boosted", "gab", false, false, false),
	COMBO_SWIFT("&9Combo: Swift", "Combo: Swift", "comboswift", false, true, false),
	BULLET_TIME("&9Bullet Time", "Bullet Time", "bullettime", false, true, false),
	GUTS("&9Guts", "Guts", "guts", false, false, false),
	CRUSH("&9Crush", "Crush", "crush", false, true, false),
	MEGA_LONGBOW("&dRARE! &9Mega Longbow", "Mega Longbow", "megalongbow", true, true, false),
	ROBINHOOD("&dRARE! &9Robinhood", "Robinhood", "robinhood", true, true, false),
	VOLLEY("&dRARE! &9Volley", "Volley", "volley", true, true, false),
	TELEBOW("&dRARE! &9Telebow", "Telebow", "telebow", true, true, false),
	PULLBOW("&dRARE! &9Pullbow", "Pullbow", "pullbow", true, true, false),
	EXPLOSIVE("&dRARE! &9Explosive", "Explosive", "explosive", true, true, false),
	TRUE_SHOT("&dRARE! &9True Shot", "True Shot", "trueshot", true, true, false),
	LUCKY_SHOT("&dRARE! &9Lucky Shot", "Lucky Shot", "luckyshot", true, true, false),
	SPRINT_DRAIN("&9Sprint Drain", "Sprint Drain", "sprintdrain", false, true, false),
	WASP("&9Wasp", "Wasp", "wasp", false, true, false),
	PIN_DOWN("&9Pin down", "Pin down", "pindown", false, true, false),
	FASTER_THAN_THEIR_SHADOW("&9Faster than their shadow", "Faster than their shadow", "fasterthantheirshadow", false, true, false),
	PUSH_COMES_TO_SHOVE("&9Push comes to shove", "Push comes to shove", "pushcomestoshove", false, true, false),
	PARASITE("&9Parasite", "Parasite", "parasite", false, true, false),
	CHIPPING("&9Chipping", "Chipping", "chipping", false, true, false),
	FLETCHING("&9Fletching", "Fletching", "fletch", false, false, false),
	SNIPER("&9Sniper", "Sniper", "sniper", false, false, false),
	SPAMMER_AND_PROUD("&9Spammer and Proud", "Spammer and Proud", "spammerandproud", false, false, false),
	JUMPSPAMMER("&9Jumpspammer", "Jumpspammer", "jumpspammer", false, false, false),
	RETROGRAVITY_MICROCOSM("&dRARE! &9Retro-Gravity Microcosm", "Retro-Gravity Microcosm", "rgm", true, true, false),
	REGULARITY("&dRARE! &9Regularity", "Regularity", "regularity", true, true, false),
	SOLITUDE("&dRARE! &9Solitude", "Solitude", "solitude", true, true, false),
	SINGULARITY("&dRARE! &9Singularity", "Singularity", "singularity", true, true, false),
	MIRROR("&9Mirror", "Mirror", "mirror", false, true, false),
	SUFFERANCE("&9Sufferance", "Sufferance", "sufferance", false, false, false),
	CRITICALLY_FUNKY("&9Critically Funky", "Critically Funky", "criticallyfunky", false, true, false),
	FRACTIONAL_RESERVE("&9Fractional Reserve", "Fractional Reserve", "fractionalreserve", false, true, false),
	NOT_GLADIATOR("&9Not Gladiator", "Not Gladiator", "notglad", false, false, false),
	PROTECTION("&9Protection", "Protection", "prot", false, false, false),
	RING_ARMOR("&9Ring Armor", "Ring Armor", "ring", false, false, false),
	PEROXIDE("&9Peroxide", "Peroxide", "pero", false, true, false),
	BOOBOO("&9Boo-boo", "Boo-boo", "booboo", false, true, false),
	REALLY_TOXIC("&9Really Toxic", "Really Toxic", "reallytoxic", false, true, false),
	NEW_DEAL("&9New Deal", "New Deal", "newdeal", false, true, false),
	HEIGHHO("&9HeighHo", "HeighHo", "heighho", false, true, false),
	GOLDEN_HEART("&9Golden Heart", "Golden Heart", "goldenheart", false, true, false),
	HEARTS("&9Hearts", "Hearts", "hearts", false, false, false),
	PRICK("&9Prick", "Prick", "prick", false, false, false),
	ELECTROLYTES("&9Electrolytes", "Electrolytes", "electrolytes", false, true, false),
	GOTTA_GO_FAST("&9Gotta go fast", "Gotta go fast", "gottagofast", false, true, false),
	COUNTEROFFENSIVE("&9CounterOffensive", "CounterOffensive", "counteroffensive", false, false, false),
	LAST_STAND("&9Last Stand", "Last Stand", "laststand", false, false, false),
	STEREO("&9Stereo", "Stereo", "stereo", false, false, false),
	MOCTEZUMA("&9Moctezuma", "Moctezuma", "moctezuma", false, false, false),
	GOLD_BUMP("&9Gold Bump", "Gold Bump", "goldbump", false, false, false),
	GOLD_BOOST("&9Gold Boost", "Gold Boost", "goldboost", false, false, false),
	SWEATY("&9Sweaty", "Sweaty", "sweaty", false, true, false),
	FREEZE("&dRARE! &9Freeze", "Freeze", "freeze", true, true, true),
	METEOR("&dRARE! &9Meteor", "Meteor", "meteor", true, true, true),
	CLEAVE("&dRARE! &9Cleave", "Cleave", "cleave", true, true, true),
	WARP("&dRARE! &9Warp", "Warp", "warp", true, true, true),
	NECROTIC("&dRARE! &9Necrotic", "Necrotic", "necrotic", true, true, true),
	SONIC("&dRARE! &9Sonic", "Sonic", "sonic", true, true, true),
	WEAK("&aUNC. &9Weak", "Weak", "weak", false, true, true),
	FRAIL("&aUNC. &9Frail", "Frail", "frail", false, true, true),
	DEFRACTION("&dRARE! &9Defraction", "Defraction", "defraction", true, true, true),
	DEVOUR("&dRARE! &9Devour", "Devour", "devour", true, true, true),
	BIPOLAR("&dRARE! &9Bipolar", "Bipolar", "bipolar", true, true, true),
	ELECTRIC_SHOCK("&dRARE! &9Electric Shock", "Electric Shock", "electricshock", true, true, true),
	HEMORRHAGE("&dRARE! &9Hemorrhage", "Hemorrhage", "hemorrhage", true, true, true),
	INFERNO("&dRARE! &9Inferno", "Inferno", "inferno", true, true, true),
	LEECH("&dRARE! &9Leech", "Leech", "leech", true, true, true),
	MEDIC("&dRARE! &9Medic", "Medic", "medic", true, true, true),
	PERSEPHONE("&dRARE! &9Persephone", "Persephone", "persephone", true, true, true),
	ROLLING_THUNDER("&dRARE! &9Rolling Thunder", "Rolling Thunder", "rollingthunder", true, true, true),
	SWARM("&dRARE! &9Swarm", "Swarm", "swarm", true, true, true),
	TERROR("&dRARE! &9Terror", "Terror", "terror", true, true, true),
	HUNTER("&aUNC. &9Hunter", "Hunter", "hunter", false, true, true),
	ELITE_HUNTER("&aUNC. &9Elite Hunter", "Elite Hunter", "elitehunter", false, true, true),
	TITAN_HUNTER("&aUNC. &9Titan Hunter", "Titan Hunter", "titanhunter", false, true, true),
	GUARD("&aUNC. &9Guard", "Guard", "guard", false, true, true),
	SHIELD("&aUNC. &9Shield", "Shield", "shield", false, true, true),
	BARRICADE("&aUNC. &9Barricade", "Barricade", "barricade", false, true, true),
	DURABLE("&aUNC. &9Durable", "Durable", "durable", false, true, true),
	ADRENALINE("&aUNC. &9Adrenaline", "Adrenaline", "adrenaline", false, true, true),
	BARBARIC("&aUNC. &9Barbaric", "Barbaric", "barbaric", false, true, true),
	COMBO_DEFENCE("&aUNC. &9Combo: Defence", "Combo: Defence", "combodefence", false, true, true),
	COMBO_MANA("&aUNC. &9Combo: Mana", "Combo: Mana", "combomana", false, true, true),
	COMBO_SLOW("&aUNC. &9Combo: Slow", "Combo: Slow", "comboslow", false, true, true),
	DESPERATE("&aUNC. &9Desperate", "Desperate", "desperate", false, true, true),
	EMBOLDENED("&aUNC. &9Emboldened", "Emboldened", "emboldened", false, true, true),
	ETHEREAL("&aUNC. &9Ethereal", "Ethereal", "ethereal", false, true, true),
	FEARMONGER("&aUNC. &9Fearmonger", "Fearmonger", "fearmonger", false, true, true),
	FORTIFY("&aUNC. &9Fortify", "Fortify", "fortify", false, true, true),
	GREED("&aUNC. &9Greed", "Greed", "greed", false, true, true),
	HOARDER("&aUNC. &9Hoarder", "Hoarder", "hoarder", false, true, true),
	LEAVE_ME_ALONE("&aUNC. &9Leave Me Alone", "Leave Me Alone", "leavemealone", false, true, true),
	MECHANIC("&aUNC. &9Mechanic", "Mechanic", "mechanic", false, true, true),
	MENDING("&aUNC. &9Mending", "Mending", "mending", false, true, true),
	PERMED("&aUNC. &9Permed", "Permed", "permed", false, true, true),
	PITPOCKET("&aUNC. &9PitPocket", "PitPocket", "pitpocket", false, true, true),
	REAPER("&aUNC. &9Reaper", "Reaper", "reaper", false, true, true),
	RESILIENT("&dRARE! &9Resilient", "Resilient", "resilient", true, true, true),
	SHIELD_BUSTER("&aUNC. &9Shield Buster", "Shield Buster", "shieldbuster", false, true, true),
	STARTING_HAND("&aUNC. &9Starting Hand", "Starting Hand", "startinghand", false, true, true),
	TANKY("&aUNC. &9Tanky", "Tanky", "tanky", false, true, true),
	ALOFT("&9Aloft", "Aloft", "aloft", false, false, true),
	ANKLEBITER("&9AnkleBiter", "AnkleBiter", "anklebiter", false, false, true),
	ANOMALY_DETECTED("&9Anomaly Detected!", "Anomaly Detected!", "anomalydetected", false, false, true),
	ANTAGONIST("&9Antagonist", "Antagonist", "antagonist", false, false, true),
	ATTENTIVE("&9Attentive", "Attentive", "attentive", false, false, true),
	BELITTLE("&9Belittle", "Belittle", "belittle", false, false, true),
	BOOM("&9BOOM!", "BOOM!", "boom", false, false, true),
	EMBALM("&9Embalm", "Embalm", "embalm", false, false, true),
	EVASIVE("&9Evasive", "Evasive", "evasive", false, false, true),
	EXTINGUISH("&9Extinguish", "Extinguish", "extinguish", false, false, true),
	GENETIC_RECONSTRUCTION("&9Genetic Reconstruction", "Genetic Reconstruction", "geneticreconstruction", false, false, true),
	HUGGABLE("&9Huggable", "Huggable", "huggable", false, false, true),
	INTIMIDATING("&9Intimidating", "Intimidating", "intimidating", false, false, true),
	NIMBLE("&9Nimble", "Nimble", "nimble", false, false, true),
	NOCTURNAL_PREDATOR("&9Nocturnal Predator", "Nocturnal Predator", "nocturnalpredator", false, false, true),
	PIERCING("&9Piercing", "Piercing", "piercing", false, false, true),
	PIN_CUSHION("&9Pin Cushion", "Pin Cushion", "pincushion", false, false, true),
	PYROTECHNIC("&9Pyrotechnic", "Pyrotechnic", "pyrotechnic", false, false, true),
	SENTINEL("&9Sentinel", "Sentinel", "sentinel", false, false, true),
	SHADOW_CLOAK("&9Shadow Cloak", "Shadow Cloak", "shadowcloak", false, false, true),
	TERRITORIAL("&9Territorial", "Territorial", "territorial", false, false, true),
	UNDERTAKER("&9Undertaker", "Undertaker", "undertaker", false, false, true),
	WHO_NEEDS_BOWS("&9Who Needs Bows?", "Who Needs Bows?", "whoneedsbows", false, false, true),
	;

	private final String displayName;
	private final String rawName;
	private final String refName;
	private final boolean isRare;
	private final boolean isUncommon;
	private final boolean isTainted;

	PitEnchant(String displayName, String rawName, String refName, boolean isRare, boolean isUncommon, boolean isTainted) {
		this.displayName = displayName;
		this.rawName = rawName;
		this.refName = refName;
		this.isRare = isRare;
		this.isUncommon = isUncommon;
		this.isTainted = isTainted;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getRawName() {
		return rawName;
	}

	public String getShortenedRawName() {
		return rawName
				.replaceAll("Combo: Perun", "Perun")
				.replaceAll("Faster than their shadow", "FTTS")
				.replaceAll("Retro-Gravity Microcosm", "RGM");
	}

//	Raw name with removed puncutation and stuff
	public String getSimpleName() {
		return rawName.replaceAll("[,.;:!?'\"\\\\]", "").replaceAll("-", " ");
	}

	public String getRefName() {
		return refName;
	}

	public boolean isRare() {
		return isRare;
	}

	public boolean isUncommon() {
		return isUncommon;
	}

	public boolean isTainted() {
		return isTainted;
	}

	public static PitEnchant getEnchantBySimpleName(String simpleName) {
		for(PitEnchant pitEnchant : values()) if(pitEnchant.getSimpleName().equalsIgnoreCase(simpleName)) return pitEnchant;
		return null;
	}

	public static PitEnchant getEnchantByRefName(String refName) {
		for(PitEnchant pitEnchant : values()) if(pitEnchant.getRefName().equalsIgnoreCase(refName)) return pitEnchant;
		return null;
	}

	public static List<String> getAllRefNames() {
		List<String> refNames = new ArrayList<>();
		for(PitEnchant pitEnchant : values()) refNames.add(pitEnchant.getRefName());
		return refNames;
	}
}
