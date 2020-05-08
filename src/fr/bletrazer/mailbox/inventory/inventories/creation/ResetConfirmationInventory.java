package fr.bletrazer.mailbox.inventory.inventories.creation;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.content.InventoryContents;

public class ResetConfirmationInventory extends ConfirmationInventoryBuilder {
	
	public static final String SUB_ID = "reset_creation_inventory";
	private static final String RESET = LangManager.getValue("string_reset_creation");
	
	public ResetConfirmationInventory(InventoryBuilder parent) {
		super("reset_creation_inventory", "§l" + RESET);
		super.setParent(parent);
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			CreationInventory inv = CreationInventory.newInventory(player.getUniqueId());
			inv.setParent(getParent().getParent());
			inv.openInventory(player);

		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

}
