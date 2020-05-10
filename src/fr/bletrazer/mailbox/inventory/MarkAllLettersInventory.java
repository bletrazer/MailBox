package fr.bletrazer.mailbox.inventory;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.sql.LetterDataSQL;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;
import fr.minuskube.inv.content.InventoryContents;

public class MarkAllLettersInventory extends ConfirmationInventoryBuilder {
	private static final String MARK_ALL_2 = LangManager.getValue("string_mark_all_as_read");
	private static final String SUB_ID = "mark_all";
	private List<LetterData> dataList;
	
	public MarkAllLettersInventory(List<LetterData> dataList, InventoryBuilder parent) {
		super(SUB_ID, "§l" + LangManager.format(MARK_ALL_2, dataList.size()) );
		super.setParent(parent);
		this.dataList = dataList;
	}


	@Override
	public void onUpdate(Player player, InventoryContents contents) {
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		return event -> {
			Boolean b = true;
			
			for (LetterData letterData : this.dataList) {
				if(LetterDataSQL.getInstance().update(letterData) ) {
					letterData.setIsRead(true);
					
				} else {
					b = false;
					break;
				}

			}
			if(b ) {
				this.returnToParent(player);
				
			} else {
				MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_error_player"));
				player.closeInventory();
			}

		};
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		return null;
	}

}
