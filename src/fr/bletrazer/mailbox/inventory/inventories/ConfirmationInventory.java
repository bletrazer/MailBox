package fr.bletrazer.mailbox.inventory.inventories;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.ConfirmationExecutors;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class ConfirmationInventory extends InventoryBuilder {
	public static final String ID = "MailBox_confirmation";
	public static final Material CONFIRMATION_MATERIAL = Material.GREEN_TERRACOTTA;
	public static final Material ANNULATION_MATERIAL = Material.RED_TERRACOTTA;
	
	private ConfirmationExecutors executors;
	
	private String confirmation = "§f§l" + LangManager.getValue("string_confirm");
	private String annulation = "§c§l" + LangManager.getValue("string_cancel");
	
	public ConfirmationInventory(String subId, String title) {
		super(ID + subId, "§l" + title, 3);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Consumer<InventoryClickEvent> consumer = this.getExecutors().onAnnulation(player, contents);
		if(consumer == null) {
			consumer = this.goBackListener(player);
		}
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(ANNULATION_MATERIAL).setName(this.annulation).build(), consumer) );
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CONFIRMATION_MATERIAL).setName(this.confirmation).build(), this.getExecutors().onConfirmation(player, contents)) );
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
	}

	private ConfirmationExecutors getExecutors() {
		return executors;
	}

	public void setExecutors(ConfirmationExecutors executors) {
		this.executors = executors;
	}
	
	public void setConfirmationMsg(String msg) {
		this.confirmation = msg;
	}

}
