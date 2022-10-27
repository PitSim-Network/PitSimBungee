package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MenuGUI extends PreparedGUI {
	public Guild guild;

	public MenuPanel menuPanel;
	public DyePanel dyePanel;
	public BuffPanel buffPanel;
	public UpgradePanel upgradePanel;
	public ShopPanel shopPanel;
	public SettingsPanel settingsPanel;

	public MenuGUI(ProxiedPlayer player, Guild guild) {
		super(player);
		this.guild = guild;

		this.menuPanel = new MenuPanel(this);
		this.dyePanel = new DyePanel(this);
		this.buffPanel = new BuffPanel(this);
		this.upgradePanel = new UpgradePanel(this);
		this.shopPanel = new ShopPanel(this);
		this.settingsPanel = new SettingsPanel(this);

		setHomePanel(menuPanel);
	}
}
