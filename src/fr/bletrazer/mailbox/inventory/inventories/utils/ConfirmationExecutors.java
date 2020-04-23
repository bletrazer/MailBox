package fr.bletrazer.mailbox.inventory.inventories.utils;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.minuskube.inv.content.InventoryContents;

public abstract class ConfirmationExecutors {
	
	public ConfirmationExecutors() {
		
	}
	
	public abstract Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents);
	public abstract Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents);
	public abstract void onUpdate(Player player, InventoryContents contents);
	
}
