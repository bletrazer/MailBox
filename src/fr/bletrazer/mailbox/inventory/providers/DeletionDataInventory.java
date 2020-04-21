package fr.bletrazer.mailbox.inventory.providers;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.builders.ConfirmationContentBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.minuskube.inv.content.InventoryContents;

public class DeletionDataInventory extends ConfirmationContentBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItem";
	
	private DataHolder holder;
	private Long dataId;
	
	public DeletionDataInventory(DataHolder dataSource, Long dataId, String InventoryTitle, InventoryProviderBuilder parent) {
		super(INVENTORY_SUB_ID, InventoryTitle);
		this.setHolder(dataSource);
		this.setDataId(dataId);
		this.setParent(parent);
		
		// ClickableItem.empty(MailBoxInventoryHandler.getInstance().generateItemRepresentation(this.getHolder().getData(this.getDataId())) )

	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			
			MailBoxController.deleteData(this.getHolder(), this.getDataId());
			this.getParent().openInventory(player);
			
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		Data data = this.getHolder().getData(this.getDataId());
		
		if(data instanceof ItemData) {
			if(((ItemData) data).isOutOfDate()) {
				MailBoxController.deleteItem(this.getHolder(), data.getId());
				contents.inventory().getParent().get().open(player);
			}
		}
	}

	public DataHolder getHolder() {
		return holder;
	}

	private void setHolder(DataHolder holder) {
		this.holder = holder;
	}
	public Long getDataId() {
		return dataId;
	}
	
	private void setDataId(Long dataId) {
		this.dataId = dataId;
	}

}
