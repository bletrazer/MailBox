package fr.bletrazer.mailbox.inventory.inventories;

import java.util.ArrayList;
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
import fr.bletrazer.mailbox.sql.SQLConnection;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class ItemInventory extends InventoryBuilder {
	private static final String TITLE = LangManager.getValue("string_menu_items");
	private static final String CLEAN = LangManager.getValue("string_clean_inbox");
	private static final String QUESTION_CLEAN = LangManager.getValue("question_clean_items");
	private static final String PERMISSION_NEEDED = LangManager.getValue("string_permission_needed");
	private static final String QUESTION_DELETE = LangManager.getValue("question_delete_item");
	private static final String RETREIVE_ALL = LangManager.getValue("string_retreive_all");

	private DataHolder dataSource;
	private List<ItemData> toShow = new ArrayList<>();

	public ItemInventory(DataHolder dataSource) {
		super("MailBox_Items", "§l" + TITLE, 5);
		this.setDataSource(dataSource);
	}

	public ItemInventory(DataHolder dataSource, InventoryBuilder parent) {
		super("MailBox_Items", "§l" + TITLE, 5);
		this.setDataSource(dataSource);
		this.setParent(parent);

	}

	private void deleteAllButton(Player player, InventoryContents contents) {
		if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.delete.self") || player.hasPermission("mailbox.item.delete.other")) {

			contents.set(4, 6, ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§4§l" + CLEAN).build(), e -> {

				if (e.getClick() == ClickType.LEFT) {
					if (SQLConnection.getInstance().isConnected()) {
						DeletionDatasInventory deletionDatasInventory = new DeletionDatasInventory(this.getDataSource(), this.getToShow().stream().collect(Collectors.toList()),
								"§4§l" + LangManager.format(QUESTION_CLEAN, this.getToShow().size()), this, true);
						deletionDatasInventory.openInventory(player);

					} else {
						MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_error_player"));
						player.closeInventory();
						return;
					}
				}
			}));

		}
	}

	private void dynamicContent(Player player, InventoryContents contents) {
		this.setToShow(DataManager.getTypeData(this.getDataSource(), ItemData.class));

		ClickableItem[] clickableItems = new ClickableItem[getToShow().size()];

		for (Integer index = 0; index < getToShow().size(); index++) {
			ItemData tempData = getToShow().get(index);

			if (tempData.isOutOfDate()) {
				MailBoxController.deleteItem(player, this.getDataSource(), tempData);
			} else {
				clickableItems[index] = ClickableItem.of(MailBoxInventoryHandler.generateItemRepresentation(tempData), e -> {
					ClickType clickType = e.getClick();

					if (clickType == ClickType.LEFT) {
						if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.recover.self") || player.hasPermission("mailbox.item.recover.other")) {
							if (!MailBoxController.recoverItem(player, getDataSource(), tempData)) {

							} else if (!getToShow().isEmpty()) {
								contents.set(4, 6, null);
							}
						} else {
							MessageUtils.sendMessage(player, MessageLevel.ERROR, PERMISSION_NEEDED);
						}

					} else if (clickType == ClickType.CONTROL_DROP || clickType == ClickType.DROP) {
						if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.delete.self") || player.hasPermission("mailbox.item.delete.other")) {
							DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), tempData, "§c§l" + QUESTION_DELETE, this);
							inv.openInventory(player);

						} else {
							MessageUtils.sendMessage(player, MessageLevel.ERROR, PERMISSION_NEEDED);
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

		if (!getToShow().isEmpty()) {
			deleteAllButton(player, contents);
		}

		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build()));

		if (!pagination.isFirst()) {
			contents.set(4, 1, this.previousPageItem(player, contents));
		}

		if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.item.recover.self") || player.hasPermission("mailbox.item.recover.other")) {
			contents.set(4, 2, generateRecoverAll(player, contents));
		}

		if (!pagination.isLast()) {
			contents.set(4, 7, this.nextPageItem(player, contents));
		}

	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		dynamicContent(player, contents);
	}

	private ClickableItem generateRecoverAll(Player player, InventoryContents contents) {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(Material.CHEST).setName("§e§l" + RETREIVE_ALL);

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			if (e.getClick() == ClickType.LEFT) {
				List<ItemData> dataList = getToShow();
				dataList.sort(DataManager.ascendingDateComparator());
				Boolean b = true;

				if (!dataList.isEmpty()) {
					for (ItemData itemData : dataList) {
						if (!MailBoxController.recoverItem(player, this.getDataSource(), itemData)) {
							b = false;
							break;
						}
					}
				}

				if (b) {
					contents.set(4, 6, null);
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
