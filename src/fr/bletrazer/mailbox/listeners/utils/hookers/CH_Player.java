package fr.bletrazer.mailbox.listeners.utils.hookers;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;

public class CH_Player extends ChatHooker {
	
	public static final String ID = "MailBox_Player_ChatHooker";
	
	public CH_Player(IdentifiersList identifiersList, InventoryBuilder parentInv) {
		super(ID, LangManager.getValue("information_ch_player_selection_start") );

		this.setExecution(event -> {
			Player ePlayer = event.getPlayer();
			String eMessage = event.getMessage();
			event.setCancelled(true);

			if (eMessage.equals("#stop")) {
				parentInv.openInventory(ePlayer);
				this.stop();
				return;
			}

			List<String> splitedMsg = Arrays.asList(eMessage.split(","));
			String wrongName = identifiersList.addAllIdentifiers(splitedMsg);

			if (wrongName == null) {
				ePlayer.sendMessage(LangManager.getValue("information_chat_selection_recipients", identifiersList.getPreviewString()));
				parentInv.openInventory(ePlayer);
				this.stop();

			} else {
				ePlayer.sendMessage((LangManager.getValue("error_chat_selection_recipients", wrongName)));

			}
		});
	}

}
