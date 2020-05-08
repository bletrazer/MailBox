package fr.bletrazer.mailbox.inventory.inventories;

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

public class DeletionDataInventory extends ConfirmationInventoryBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItem";
	
	private DataHolder dataSource;
	private Data data;
	private Boolean update = false;
	
	public DeletionDataInventory(DataHolder dataSource, Data data, String InventoryTitle, InventoryBuilder parent) {
		super(INVENTORY_SUB_ID, InventoryTitle);
		this.setDataSource(dataSource);
		this.setData(data);
		this.setParent(parent);
		if(data instanceof ItemData ) {
			update = true;
		}
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			if(e.getClick() == ClickType.LEFT ) {
				MailBoxController.deleteData(this.getDataSource(), this.getData() );
				this.returnToParent(player);
			}
			
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		if(update ) {
			ItemData tempData = (ItemData) this.getData();
			
			if(tempData.isOutOfDate()) {
				MailBoxController.deleteItem(this.getDataSource(), tempData);
				contents.inventory().getParent().get().open(player);
			}
		}
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public DataHolder getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}

}
