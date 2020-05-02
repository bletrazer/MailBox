package fr.bletrazer.mailbox.listeners.utils.hookers;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;

public class CH_SimpleMessage extends ChatHooker {
	
	public CH_SimpleMessage(String id, String startMsg, StringBuilder content, InventoryBuilder parentInv) {
		super(id, startMsg);
		
		this.setExecution(event -> {
			Player ePlayer = event.getPlayer();
			String eMessage = event.getMessage();
			event.setCancelled(true);

			if (eMessage.equals("#stop")) {
				parentInv.openInventory(ePlayer);
				this.stop();
				return;
			}
			
			if(content.toString() != null && !content.toString().isEmpty() ) {
				content.delete(0, content.length());
				
			}
			
			content.append(eMessage);
			
			this.stop();
			parentInv.openInventory(ePlayer);
			
				
		});
	}

}
