package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.creation.CreationInventory;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class MailBoxInventory extends InventoryBuilder {
	private static final String LETTERS = LangManager.getValue("string_menu_letters");
	private static final String ITEMS = LangManager.getValue("string_menu_items");
	private static final String CREATION = LangManager.getValue("string_menu_creation");
	
	private DataHolder dataSource;
	
	public MailBoxInventory(DataHolder dataSource) {
		super("MailBox_Principal", "§lMenu principal", 3);
		this.setDataSource(dataSource);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(Material.LECTERN).setName("§f§l"+LETTERS).build(), e -> {
			if(e.getClick() == ClickType.LEFT ) {
				LetterInventory inv = new LetterInventory(this.getDataSource(), this);
				inv.openInventory(player);
			}
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(Material.CHEST).setName("§f§l"+ITEMS).build(), e ->  {
			if(e.getClick() == ClickType.LEFT ) {
				ItemInventory inv = new ItemInventory(this.getDataSource(), this);
				inv.openInventory(player);
			}
		}));
		
		if(player.hasPermission("mailbox.send.standard") ) {
			contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(Material.HOPPER).setName("§f§l"+CREATION).build(), e ->  {
				if(e.getClick() == ClickType.LEFT ) {
					CreationInventory inv = CreationInventory.getInventory(player.getUniqueId());
					inv.setParent(this);
					inv.openInventory(player);
					
				}
				
			}));
		}
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