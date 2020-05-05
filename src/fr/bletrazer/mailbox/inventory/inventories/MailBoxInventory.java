package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class MailBoxInventory extends InventoryBuilder {
	public static Material LETTER_MENU_MATERIAL = Material.LECTERN;
	public static Material ITEM_MENU_MATERIAL = Material.CHEST;
	public static Material SEND_LETTER_MATERIAL = Material.HOPPER;
	
	private DataHolder dataSource;
	
	public MailBoxInventory(DataHolder dataSource) {
		super("MailBox_Principal", "§lMenu principal", 3);
		this.setDataSource(dataSource);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(LETTER_MENU_MATERIAL).setName("§f§l"+LangManager.getValue("string_menu_letters")).build(), e -> {
			LetterInventory inv = new LetterInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(ITEM_MENU_MATERIAL).setName("§f§l"+LangManager.getValue("string_menu_items")).build(), e ->  {
			ItemInventory inv = new ItemInventory(this.getDataSource(), this);
			inv.openInventory(player);
		}));
		
		contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(SEND_LETTER_MATERIAL).setName("§f§l"+LangManager.getValue("string_menu_creation")).build(), e ->  {
			LetterDataCreatorInventory inv = new LetterDataCreatorInventory();
			inv.setParent(this);
			inv.openInventory(player);
			
		}));
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		
	}

	private DataHolder getDataSource() {
		return dataSource;
	}

	private void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}
	
}