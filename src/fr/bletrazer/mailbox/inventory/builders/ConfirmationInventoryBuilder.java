package fr.bletrazer.mailbox.inventory.builders;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public abstract class ConfirmationInventoryBuilder extends InventoryBuilder {
	public static Material CONFIRMATION_MATERIAL = Material.RED_TERRACOTTA;
	public static Material ANNULATION_MATERIAL = Material.GREEN_TERRACOTTA;
	
	//TODO rajouter un preview ?
	
	public ConfirmationInventoryBuilder(String subId, String title) {
		super("MailBox_Confirmation_" + subId, title, 3);
		
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Consumer<InventoryClickEvent> consumer = this.goBackListener(player);
		if(this.onAnnulation(player, contents) != null) {
			consumer = this.onAnnulation(player, contents);
		}
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(ANNULATION_MATERIAL).setName("§f§lAnnuler").build(), consumer) );
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CONFIRMATION_MATERIAL).setName("§4§lConfirmer").build(), onConfirmation(player, contents)) );
		
	}
	
	public abstract Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents);
	public abstract Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents);
	public abstract void onUpdate(Player player, InventoryContents contents);

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.onUpdate(player, contents);
	}
	
}
