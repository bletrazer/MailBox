package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.LetterCreator;
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
		
		contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(SEND_LETTER_MATERIAL).setName("§f§l"+LangManager.getValue("string_send_letter")).build(), e ->  {
			ClickType click = e.getClick();
			ItemStack cursor = e.getCursor();
			
			if (click == ClickType.LEFT ) {
				if(!LetterCreator.isCreatingLetter(player)) {
					if(cursor.getType() == Material.WRITTEN_BOOK && cursor.hasItemMeta() ) { // avancé
						if(player.getInventory().firstEmpty() > -1) {
							BookMeta meta = (BookMeta) cursor.getItemMeta();
							player.getInventory().addItem(cursor);
							e.setCursor(null);
							player.closeInventory();
							LetterCreator creator = new LetterCreator();
							creator.setContent(meta.getPages() );
							creator.startCreation(player);
							
						} else {
							player.sendMessage(LangManager.getValue("string_not_enought_space"));
						}
					} else {//simple
						player.closeInventory();
						LetterCreator creator = new LetterCreator();
						creator.startCreation(player);
					}
				}
				
			}
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