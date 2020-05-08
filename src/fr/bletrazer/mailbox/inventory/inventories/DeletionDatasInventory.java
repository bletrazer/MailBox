package fr.bletrazer.mailbox.inventory.inventories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.minuskube.inv.content.InventoryContents;

public class DeletionDatasInventory extends ConfirmationInventoryBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItems";
	
	private DataHolder dataSource;
	private List<Data> dataList = new ArrayList<>();
	private Boolean doUpdate = false;
	
	public DeletionDatasInventory(DataHolder dataSource, List<Data> dataList, String inventoryTitle, InventoryBuilder parent, Boolean doUpdate) {
		super(INVENTORY_SUB_ID, inventoryTitle);
		this.setDataSource(dataSource);
		this.setDataList(dataList);
		this.setParent(parent);
		this.setDoUpdate(doUpdate);

	}
	
	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			if(e.getClick() == ClickType.LEFT ) {
				if(MailBoxController.deleteDatas(player, this.getDataSource(), getDataList()) ) {
					this.returnToParent(player);
					
				} else {
					player.closeInventory();
				}
				
			}
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		if(this.getDoUpdate() ) {
			Iterator<Data> it = this.getDataList().iterator();
	
			while (it.hasNext()) {
				Data data = it.next();
	
				if (data != null) {
					if (data instanceof ItemData ) {
						ItemData tempData = (ItemData) data;
						
						if(tempData.isOutOfDate() ) {
							if(MailBoxController.deleteItem(player, this.getDataSource(), tempData) ) {
								if (this.getDataList().isEmpty()) {
									this.returnToParent(player);
			
								}
							} else {
								player.closeInventory();
							}
							it.remove();
		

						}
					}
				} else {
					it.remove();
				}
			}
		}
	}

	public DataHolder getDataSource() {
		return this.dataSource;
	}

	private void setDataSource(DataHolder holder) {
		this.dataSource = holder;
	}
	
	public List<Data> getDataList() {
		return dataList;
	}

	public void setDataList(List<Data> dataList) {
		this.dataList = dataList;
	}

	public Boolean getDoUpdate() {
		return doUpdate;
	}

	public void setDoUpdate(Boolean doUpdate) {
		this.doUpdate = doUpdate;
	}
}
