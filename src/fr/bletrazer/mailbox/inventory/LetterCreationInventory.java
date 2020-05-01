package fr.bletrazer.mailbox.inventory;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.minuskube.inv.content.InventoryContents;

public class LetterCreationInventory extends InventoryBuilder{
	
	public static final String ID = "MailBox_letter_creation";
	
	public LetterCreationInventory() {
		super(ID, "§l"+LangManager.getValue("string_menu_letter_creation"), 3);
		
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {//TODO PlayerChatInformationSelector && menu creation lettres
		
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		
	}
	
	
	
}