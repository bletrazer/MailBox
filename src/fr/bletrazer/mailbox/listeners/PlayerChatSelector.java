package fr.bletrazer.mailbox.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.bletrazer.mailbox.inventory.providers.utils.IdentifiableAuthors;

public class PlayerChatSelector implements Listener {
	
	private static List<UUID> activity = new ArrayList<>();
	
	private Player player;
	private IdentifiableAuthors identifiableAuthors;
	private InventoryProviderBuilder parent;
	
	public PlayerChatSelector(IdentifiableAuthors identifiableAuthors, InventoryProviderBuilder parent) {
		this.setAuthorFilter(identifiableAuthors);
		this.setParent(parent);
	}
	
	public PlayerChatSelector(IdentifiableAuthors identifiableAuthors) {
		this.setAuthorFilter(identifiableAuthors);
	}
	
	public void start(Player player) {
		this.setPlayer(player);
		getActivity().add(player.getUniqueId());
		this.getPlayer().sendMessage("Entrez le nom du/des joueurs. \"#stop\" pour annuler la selection.");
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		
	}
	
	public void stop(){
		getActivity().remove(player.getUniqueId());
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	private void TryOpenInventory() {
		if(this.getParent() != null) {
			this.getParent().openInventory(this.getPlayer() );
			
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onPlayerChatSelection(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		
		if(player.equals(this.getPlayer()) ) {
			event.setCancelled(true);
			
			if(msg.equals("#stop")) {
				this.TryOpenInventory();
				this.stop();
				return;
			}
			
			List<String> splitedMsg = Arrays.asList(msg.split(",") );
			String wrongName = this.getAuthorFilter().addAllIdentifiers(splitedMsg);
			
			if(wrongName == null) {
				player.sendMessage("Vous avez choisit de cibler:\n" +  this.getAuthorFilter().getPreview().toString().replace("[", "").replace("]", ""));

				this.TryOpenInventory();
				this.stop();
				
			} else {
				player.sendMessage("Action impossible: le joueur " + wrongName + " n'a pas été trouvé, veuillez réessayer.");
				
			}

		}
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public InventoryProviderBuilder getParent() {
		return parent;
	}

	public void setParent(InventoryProviderBuilder parent) {
		this.parent = parent;
	}

	public IdentifiableAuthors getAuthorFilter() {
		return identifiableAuthors;
	}

	private void setAuthorFilter(IdentifiableAuthors identifiableAuthors) {
		this.identifiableAuthors = identifiableAuthors;
	}

	private static List<UUID> getActivity() {
		return activity;
	}
	
	public static Boolean isUsingPCS(Player player) {
		return getActivity().contains(player.getUniqueId());
	}
}
