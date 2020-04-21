package fr.bletrazer.mailbox.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.playerManager.PlayerManager;
/**
 * Charge en mémoire les données des joueurs lors de leurs connexion
 * @author Bletrazer
 *
 */
public class JoinListener implements Listener {

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		MailBoxController.load(player.getUniqueId());
		
		PlayerManager.getInstance().load(player);
	}
	
}