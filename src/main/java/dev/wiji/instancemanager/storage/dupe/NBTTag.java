package dev.wiji.instancemanager.storage.dupe;

public enum NBTTag {

	ITEM_UUID("pr-uuid"),
	RANODM_UUID("pr-random-uuid"),
	PIT_ENCHANTS("pr-enchants"),
	PIT_ENCHANT_ORDER("pr-enchant-order"),
	ITEM_JEWEL_ENCHANT("pr-jewel-enchant"),
	ITEM_TOKENS("pr-token-num"),
	ITEM_RTOKENS("pr-rare-token-num"),
	ITEM_ENCHANTS("pr-enchant-num"),
	PLAYER_KILLS("pr-player-kills"),
	BOT_KILLS("pr-bot-kills"),
	JEWEL_KILLS("pr-jewel-kills"),
	IS_JEWEL("pr-isjewel"),
	IS_GEMMED("pr-isgemmed"),
	UNDROPPABLE("pr-undroppable"),
	DROP_CONFIRM("pr-dropconfirm"),
	ORIGINAL_COLOR("pr-original-color"),
	CURRENT_LIVES("pr-current-lives"),
	MAX_LIVES("pr-max-lives"),
	IS_FEATHER("pr-isfeather"),
	IS_CORRUPTED_FEATHER("pr-iscfeather"),
	IS_TOKEN("pr-istoken"),
	IS_VILE("pr-isvile"),
	IS_SHARD("pr-isshard"),
	IS_GEM("pr-isgem"),
	IS_GHELMET("pr-isghelm"),
	GHELMET_UUID("pr-ghelm-uuid"),
	GHELMET_GOLD("pr-ghelm-gold"),
	GHELMET_ABILITY("pr-ghelm-ability"),
	IS_YUMMY_BREAD("pr-is-yummy-bread"),
	IS_VERY_YUMMY_BREAD("pr-is-very-yummy-bread2"),
	IS_PREMADE("pr-ispremade"),
	IS_VENOM("pr-isvenom"),
	TAINTED_TIER("pr-tainted-tier"),
	POTION_IDENTIFIER("pr-potion-identifier"),
	POTION_POTENCY("pr-potion-potency"),
	POTION_DURATION("pr-potion-duration"),
	ZOMBIE_FLESH("pr-zombie-flesh"),
	SKELETON_BONE("pr-skeleton-bone"),
	CREEPER_POWDER("pr-creeper-powder"),
	SPIDER_EYE("pr-spider-eye"),
	CAVESPIDER_EYE("pr-cavespider-eye"),
	MAGMACUBE_CREAM("pr-magmacube-cream"),
	PIGMAN_PORK("pr-pigman-pork"),
	WITHER_SKELETON_SKULL("pr-wither-skull"),
	GOLEM_INGOT("pr-golem-ingot"),
	ENDERMAN_PEARL("pr-enderman-pearl"),
	SAVED_PANTS_COLOR("pr-saved-color"),
	IS_TAINTED_SOUL("pr-tainted-soul"),
	IS_SPLASH_POTION("pr-is-splash-potion"),
	CANNOT_PICKUP("pr-cannot-pickup");

	private final String ref;

	NBTTag(String ref) {

		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}
}