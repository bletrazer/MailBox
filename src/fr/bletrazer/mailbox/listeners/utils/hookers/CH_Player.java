package fr.bletrazer.mailbox.listeners.utils.hookers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;

public class CH_Player extends ChatHooker {
	
	public static final String ID = "MailBox_Player_ChatHooker";
	private static Map<UUID, CH_Player> map = new HashMap<>();
	
	private IdentifiersList idList;

	public CH_Player(IdentifiersList identifiersList, InventoryBuilder parentInv) {
		super(ID);
		this.setIdList(identifiersList);

		this.setExecution(event -> {
			Player ePlayer = event.getPlayer();
			String eMessage = event.getMessage();
			event.setCancelled(true);

			if (eMessage.equals("#stop")) {
				parentInv.returnToParent(ePlayer);
				this.stop();
				return;
			}

			List<String> splitedMsg = Arrays.asList(eMessage.split(","));
			String wrongName = this.getIdList().addAllIdentifiers(splitedMsg);

			if (wrongName == null) {
				ePlayer.sendMessage(LangManager.getValue("information_chat_selection_recipients",
						this.getIdList().getPreviewString()));
				parentInv.returnToParent(ePlayer);
				this.stop();

			} else {
				ePlayer.sendMessage((LangManager.getValue("error_chat_selection_recipients", wrongName)));

			}
		});
	}

	public IdentifiersList getIdList() {
		return idList;
	}

	public void setIdList(IdentifiersList idList) {
		this.idList = idList;
	}

	@Override
	public void load(UUID uuid) {
		map.put(uuid, this);
	}

	@Override
	public void unload(UUID uuid) {
		map.remove(uuid);
	}
	
	public static CH_Player get(UUID uuid) {
		return map.get(uuid);
	}

}
