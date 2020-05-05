package fr.bletrazer.mailbox.listeners.utils.hookers;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.listeners.utils.AbstractDuration;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.utils.LangManager;

public class CH_Duration extends ChatHooker {

	public static final String ID = "MailBox_Player_ChatHooker";

	public CH_Duration(AbstractDuration absDur, InventoryBuilder parentInv) {
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

			try {
				String toCheck = eMessage.replace(" ", "");
				
				String prefix = toCheck.contains("D") ? "P" : "PT";
				String subD = toCheck.replace("D", "DT");
				String strD = prefix + subD;

				absDur.setDuration(Duration.parse(strD));
				
				parentInv.openInventory(ePlayer);
				this.stop();

			} catch (DateTimeParseException e) {
				ePlayer.sendMessage("Impossible de transformer \"" + eMessage + "\" en durée, veuillez réessayer.");
				
			}
		});
	}

}