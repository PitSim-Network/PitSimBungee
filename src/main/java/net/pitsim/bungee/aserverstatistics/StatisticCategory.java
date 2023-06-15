package net.pitsim.bungee.aserverstatistics;

public enum StatisticCategory {
	OVERWORLD_PVP("Overworld PvP", "PvP"),
	OVERWORLD_STREAKING("Overworld Streaking", "Streaking"),
	DARKZONE_VS_PLAYER("Darkzone Player vs Player", "DZ PvP"),
	DARKZONE_VS_MOB("Darkzone Player vs Mob", "DZ PvM"),
	DARKZONE_VS_BOSS("Darkzone Player vs Boss", "DZ PvB"),
	;

	private final String displayName;
	private final String shorthandDisplay;

	StatisticCategory(String displayName, String shorthandDisplay) {
		this.displayName = displayName;
		this.shorthandDisplay = shorthandDisplay;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getShorthandDisplay() {
		return shorthandDisplay;
	}
}
