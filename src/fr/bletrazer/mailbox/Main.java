package fr.bletrazer.mailbox;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import fr.bletrazer.mailbox.commands.Cmd_Mailbox;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.JoinListener;
import fr.bletrazer.mailbox.listeners.QuitListener;
import fr.bletrazer.mailbox.playerManager.PlayerManager;
import fr.bletrazer.mailbox.sql.SQLConnection;
import fr.minuskube.inv.InventoryManager;

public class Main extends JavaPlugin {
	private static InventoryManager manager;
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
		
		if(SQLConnection.getInstance().getConnection() != null && SQLConnection.getInstance().isConnected() ) {
		
			manager = new InventoryManager(this);
			manager.init();
			PlayerManager.getInstance().init();
			
			this.getCommand(Cmd_Mailbox.CMD_LABEL).setExecutor(new Cmd_Mailbox());
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