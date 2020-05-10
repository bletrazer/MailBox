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
	 * TODO read state non enregistré dans la db quand on modifie tout en meme temps -> 
	 *  -> faire une method updateAll dans SQL
	 *  quand on recup un item -> actualize inventaire + ne pas pouvoir recup l'item si deja été récup (anti spam bug)
	 * 
	 * 
	 * 
	 * 
	 * TODO menu lettre non envoyés/en cours *-}
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

		SQLConnection.getInstance().connect(SQLConnection.SGBD_TYPE_ROOT, this.getConfig().getString("database.host"),
				this.getConfig().getString("database.database"), this.getConfig().getString("database.user"),
				this.getConfig().getString("database.password"));

		this.getCommand(Cmd_Mailbox.CMD_LABEL).setExecutor(new Cmd_Mailbox());
		
		if (SQLConnection.getInstance().getConnection() != null && SQLConnection.getInstance().isConnected() ) {
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