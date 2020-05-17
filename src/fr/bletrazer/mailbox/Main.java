package fr.bletrazer.mailbox;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.commands.Cmd_Mailbox;
import fr.bletrazer.mailbox.listeners.JoinListener;
import fr.bletrazer.mailbox.listeners.QuitListener;
import fr.bletrazer.mailbox.playerManager.PlayerManager;
import fr.bletrazer.mailbox.sql.SQLConnection;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.InventoryManager;

public class Main extends JavaPlugin {
	private static InventoryManager manager;

	/*
	 * completer commandes /mailbox check avec informations objets -> fait
	 * checker la durée des objet lors du chargement (et supprimé si <0) -> fait
	 * ajouter lign useSSL dans config -> fait
	 * ajouter boutton ajout de ligne de message -> fait
	 * checker si sql rollback fonctionne (permissions)
	 * 
	 * TODO menu lettre non envoyés/en cours
	 */

	public static InventoryManager getManager() {
		return manager;
	}

	private static Main main;

	public static Main getInstance() {
		return main;
	}

	@Override
	public void onEnable() {
		Main.main = this;
		this.saveDefaultConfig();
		LangManager.load();

		SQLConnection.getInstance().setJdbc(SQLConnection.SGBD_TYPE_ROOT).setHost(this.getConfig().getString("database.host")).setDatabase(this.getConfig().getString("database.database"))
				.setUser(this.getConfig().getString("database.user")).setPassword(this.getConfig().getString("database.password")).connect();

		this.getCommand(Cmd_Mailbox.CMD_LABEL).setExecutor(new Cmd_Mailbox());

		if (SQLConnection.getInstance().getConnection() != null && SQLConnection.getInstance().isConnected()) {
			manager = new InventoryManager(this);
			manager.init();
			PlayerManager.getInstance().init();
			MailBoxController.initialize();

			this.registerListeners();

		} else {
			this.getLogger().log(Level.SEVERE, LangManager.getValue("connection_needed"));

		}

	}

	@Override
	public void onDisable() {

	}

	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
		this.getServer().getPluginManager().registerEvents(new QuitListener(), this);

	}

}