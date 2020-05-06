package fr.bletrazer.mailbox.listeners.utils.hookers;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;

public class CH_Duration extends ChatHooker {

	public static final String ID = "MailBox_Player_ChatHooker";

	public CH_Duration(StringBuilder sb, InventoryBuilder parentInv) {
		super(ID, LangManager.getValue("information_ch_duration_start"));

		this.setExecution(event -> {
			Player ePlayer = event.getPlayer();
			String eMessage = event.getMessage();
			event.setCancelled(true);

			if (eMessage.equals("#stop")) {
				parentInv.openInventory(ePlayer);
				this.stop();
				return;
			}

				Duration dur = transform(eMessage);
				
				if(dur != null) {
					if(sb.toString() != null && !sb.toString().isEmpty() ) {
						sb.delete(0, sb.length());
						
					}
					
					if(dur.isZero() ) {
						sb.append("infini");
						
					} else {
						sb.append(eMessage);
					}
					parentInv.openInventory(ePlayer);
					this.stop();
					
				} else {
					MessageUtils.sendMessage(ePlayer, MessageLevel.ERROR, LangManager.getValue("error_parsing_duration") );
					
				}

		});
	}
	
	public static Duration transform(String str) {
		Duration res = null;

		try {
			String toCheck = str.replace(" ", "");
			String part1 = toCheck.contains("D") ? "P" : "PT";
			String part2 = toCheck;
			if(toCheck.contains("H") || str.contains("M") || toCheck.contains("S") ) {
				part2 = toCheck.replace("D", "DT");
			}
			
			String strD = part1 + part2;
			
			res = Duration.parse(strD);
			
		} catch(DateTimeParseException e) {
			
		}	
		return res;
	}

}