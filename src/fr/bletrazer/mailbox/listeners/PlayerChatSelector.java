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
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiableAuthors;
import fr.bletrazer.mailbox.lang.LangManager;

public class PlayerChatSelector implements Listener {
	
	private static List<UUID> activity = new ArrayList<>();
	
	private Player player;
	private IdentifiableAuthors identifiableAuthors;
	private InventoryBuilder parent;
	
	public PlayerChatSelector(IdentifiableAuthors identifiableAuthors, InventoryBuilder parent) {
		this.setAuthorFilter(identifiableAuthors);
		this.setParent(parent);
	}
	
	public PlayerChatSelector(IdentifiableAuthors identifiableAuthors) {
		this.setAuthorFilter(identifiableAuthors);
	}
	
	public void start(Player player) {
		this.setPlayer(player);
		getActivity().add(player.getUniqueId());
		this.getPlayer().sendMessage(LangManager.getValue("error_chat_selection_recipients"));
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
			String wrongName = this.getAuthorFilter().addAllIdentifiers(player.getName(), splitedMsg);
			
			if(wrongName == null) {
				player.sendMessage(LangManager.getValue("information_chat_selection_recipients", this.getAuthorFilter().getPreviewString() ) );

				this.TryOpenInventory();
				this.stop();
				
			} else {
				player.sendMessage((LangManager.getValue("error_chat_selection_recipients", wrongName)) );
				
			}
		}
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public InventoryBuilder getParent() {
		return parent;
	}

	public void setParent(InventoryBuilder parent) {
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
