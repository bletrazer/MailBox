package fr.bletrazer.mailbox.inventory.inventories;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.DataManager;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.MailBoxInventoryHandler;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class ItemInventory extends InventoryBuilder {
	
	public static Material RULES_MATERIAL = Material.WRITABLE_BOOK;
	public static Material RECOVER_ALL_MATERIAl = Material.CHEST;
	
	private DataHolder dataSource;
	private List<ItemData> toShow;
	
	public ItemInventory(DataHolder dataSource) {
		super("MailBox_Items", "§l"+LangManager.getValue("string_menu_items"), 5);
		this.setDataSource(dataSource);
	}
	
	public ItemInventory(DataHolder dataSource, InventoryBuilder parent) {
		super("MailBox_Items", "§l"+LangManager.getValue("string_menu_items"), 5);
		this.setDataSource(dataSource);
		this.setParent(parent);
		
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		this.setToShow(DataManager.getTypeData(this.getDataSource(), ItemData.class) );
		getToShow().sort(DataManager.ascendingDateComparator().reversed());
		
		if(this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.delete.self") || player.hasPermission("mailbox.item.delete.other") ) {
			
			if(getToShow().size() > 0) {
				List<Long> listDataId = getToShow().stream().map(ItemData::getId).collect(Collectors.toList());
				
				contents.set(4,  6, ClickableItem.of(new ItemStackBuilder(MailBoxInventoryHandler.DELETE_ALL_MATERIAL).setName("§4§l"+LangManager.getValue("string_clean_inbox")).build(), e -> {

					if(e.getClick() == ClickType.LEFT ) {
						DeletionDatasInventory deletionDatasInventory = new DeletionDatasInventory(this.getDataSource(), listDataId, "§4§l" + LangManager.getValue("question_clean_items", listDataId.size()), this);
						deletionDatasInventory.openInventory(player);
						
					}
				}));
				
			}
		}
		
		ClickableItem[] clickableItems = new ClickableItem[getToShow().size()];
		
		for(Integer index = 0; index < getToShow().size(); index ++ ) {
			ItemData tempData = getToShow().get(index);
			
			if(tempData.isOutOfDate() ) {
				MailBoxController.deleteItem(this.getDataSource(), tempData);
				
			} else {
				clickableItems[index] = ClickableItem.of(MailBoxInventoryHandler.generateItemRepresentation(tempData),
						e -> {
							ClickType clickType = e.getClick();
							
							if (clickType == ClickType.LEFT) {

								if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.recover.self") || player.hasPermission("mailbox.item.recover.other")) {
									
									if(!MailBoxController.recoverItem(player, getDataSource(), tempData) ) {
										MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_not_enought_space"));
									}
									
								} else {
									MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_permission_needed"));
								}

							} else if (clickType == ClickType.CONTROL_DROP || clickType == ClickType.DROP) {
								if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.delete.self") || player.hasPermission("mailbox.item.delete.other")) {
									DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), tempData.getId(), "§c§l" + LangManager.getValue("question_delete_item"), this);
									inv.openInventory(player);

								} else {
									MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_permission_needed"));
								}
							}

						});

			}
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		this.dynamicContent(player, contents);
		
		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(MailBoxInventoryHandler.BORDER_MATERIAL).setName(" ").build() ) );
		
		if (!pagination.isFirst()) {
			contents.set(4, 1, this.previousPageItem(player, contents));
		}
		
		if(this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.recover.self") || player.hasPermission("mailbox.item.recover.other") ) {
			contents.set(4, 2, generateRecoverAll(player, contents) );
		}
			
		if (!pagination.isLast()) {
			contents.set(4, 7, this.nextPageItem(player, contents));
		}
		
		contents.set(4, 0, this.goBackItem(player) );
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		dynamicContent(player, contents);
	}
	
	private ClickableItem generateRecoverAll(Player player, InventoryContents contents) {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(RECOVER_ALL_MATERIAl).setName("§e§l" + LangManager.getValue("string_retreive_all"));

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			if(e.getClick() == ClickType.LEFT ) {
				for (ItemData itemData : DataManager.getTypeData(this.getDataSource(), ItemData.class)) {
					if (!MailBoxController.recoverItem(player, this.getDataSource(), itemData) ) {
						MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_not_enought_space"));
						break;
					}
				}
			}
		});
	}

	public DataHolder getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}

	public List<ItemData> getToShow() {
		return toShow;
	}

	public void setToShow(List<ItemData> toShow) {
		this.toShow = toShow;
	}
	
}
