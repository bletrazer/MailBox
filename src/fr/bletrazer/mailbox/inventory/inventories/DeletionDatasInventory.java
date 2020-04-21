package fr.bletrazer.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class DeletionDatasInventory extends ConfirmationContentBuilder {
	public static final String INVENTORY_SUB_ID = "deleteItems";
	
	private DataHolder holder;
	private List<Long> dataIdList = new ArrayList<>();
	
	public DeletionDatasInventory(DataHolder dataSource, List<Long> listDataId, String inventoryTitle, InventoryProviderBuilder parent) {
		super(INVENTORY_SUB_ID, inventoryTitle);
		this.setHolder(dataSource);
		this.setDataIdList(listDataId);
		this.setParent(parent);

	}
	
	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return e -> {
			for(Long id : this.getDataIdList() ) {
				MailBoxController.deleteData(this.getHolder(), id);
				
			}
			
			this.getParent().openInventory(player);
			
		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		Iterator<Long> it = this.getDataIdList().iterator();

		while (it.hasNext()) {
			Long id = it.next();
			Data data = this.getHolder().getData(id);

			if (data != null) {
				if (data instanceof ItemData && ((ItemData) data).isOutOfDate()) {
					MailBoxController.deleteItem(this.getHolder(), data.getId());
					it.remove();

					if (this.getDataIdList().isEmpty()) {
						contents.inventory().getParent().get().open(player);

					}
				}
			} else {
				it.remove();
			}

		}
	}

	public DataHolder getHolder() {
		return holder;
	}

	private void setHolder(DataHolder holder) {
		this.holder = holder;
	}
	
	public List<Long> getDataIdList() {
		return dataIdList;
	}

	public void setDataIdList(List<Long> dataIdList) {
		this.dataIdList = dataIdList;
	}
}
