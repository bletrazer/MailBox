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
			
			if(splitedMsg.size() == 1 || splitedMsg.size() > 1 && ePlayer.hasPermission("mailbox.send.announce") ) {
				String wrongName = identifiersList.addAllIdentifiers(splitedMsg);
	
				if (wrongName == null) {
					ePlayer.sendMessage(LangManager.getValue("information_chat_selection_recipients", identifiersList.getPreviewString()));
					parentInv.openInventory(ePlayer);
					this.stop();
	
				} else if (wrongName.equals(ePlayer.getName()) ) {
					ePlayer.sendMessage(LangManager.getValue("string_player_not_yourself"));
					
				} else {
					ePlayer.sendMessage(LangManager.getValue("string_player_not_found", wrongName));
	
				}
				
			} else {
				ePlayer.sendMessage(LangManager.getValue("string_permission_needed"));
				
			}
		});
	}

}
