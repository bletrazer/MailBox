package fr.bletrazer.mailbox.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;
import fr.bletrazer.mailbox.DataManager.factories.LetterDataFactory;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.PlayerSelectorInventory;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_SimpleMessage;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class LetterCreationInventory extends InventoryBuilder {

	public static final String ID = "MailBox_letter_creation";

	private IdentifiersList recipients;
	private StringBuilder object = new StringBuilder();
	private StringBuilder content = new StringBuilder();

	public LetterCreationInventory() {
		super(ID, "§l" + LangManager.getValue("string_menu_letter_creation"), 3);

	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {// TODO
		if(this.getRecipients() == null) {
			this.setRecipients(new IdentifiersList(player.getName()) );
		}
		
		contents.set(1, 1,
				ClickableItem.of(new ItemStackBuilder(Material.WRITABLE_BOOK).setName("§e§l" + "Message")
						.setAutoFormatingLore(this.getContent().toString().isEmpty() ? "Aucun message" : content.toString(), 35)
						.build(), e -> {
							ClickType click = e.getClick();
							ChatHooker chCheck = ChatHooker.get(player.getUniqueId());

							if (click == ClickType.LEFT) {
								if (chCheck == null) {
									player.closeInventory();
									CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterContent_ChatHooker",
											LangManager.getValue("string_ch_letter_content_start"), this.getContent(),
											this);
									ch.start(player);

								}

							} else if (click == ClickType.RIGHT) {
								if (chCheck == null) {
									player.closeInventory();
									CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterObject_ChatHooker",
											LangManager.getValue("string_ch_letter_object_start"), this.getObject(),
											this);
									ch.start(player);
								}
							} else if (click == ClickType.CONTROL_DROP) {

							}

						}));

		contents.set(1, 4,
				ClickableItem.of(new ItemStackBuilder(Material.ITEM_FRAME).setName("§e§l" + "Objet")
						.setAutoFormatingLore(this.getObject().toString().isEmpty() ? "Aucun objet" : this.getObject().toString(), 35)
						.build(), e -> {
							ClickType click = e.getClick();
							ChatHooker chCheck = ChatHooker.get(player.getUniqueId());

							if (chCheck == null) {
								if (click == ClickType.LEFT) {

									player.closeInventory();
									CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterObject_ChatHooker",
											LangManager.getValue("string_ch_letter_object_start"), this.getObject(),
											this);
									ch.start(player);
								} else if (click == ClickType.RIGHT) {

								} else if (click == ClickType.CONTROL_DROP) {

								}

							}

						}));

		contents.set(1, 7, ClickableItem.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("§e§l" + "Destinataires")
				.setLore(this.getRecipients().getPreviewLore())
				.build(), e -> {
					ClickType click = e.getClick();
					ChatHooker chCheck = ChatHooker.get(player.getUniqueId());
					if (chCheck == null) {
						if (click == ClickType.LEFT) {
							PlayerSelectorInventory selector = new PlayerSelectorInventory(this.getRecipients(),
									"§l" + "Choix du/des destinataires" + ":", this);
							selector.openInventory(player);

						} else if (click == ClickType.RIGHT) {

						} else if (click == ClickType.CONTROL_DROP) {

						}
					}
				}));
		
		if (!this.getObject().toString().isEmpty() && !this.getContent().toString().isEmpty() && !this.getRecipients().getPlayerList().isEmpty()) {
			contents.set(2, 8, ClickableItem.of(new ItemStackBuilder(Material.FEATHER)
					.setName("§e§l" + "CLick pour envoyer").build(), e -> {
						ClickType click = e.getClick();
					
						if (click == ClickType.LEFT) {
							LetterType type = this.getRecipients().getPlayerList().size() > 1 ? LetterType.ANNOUNCE : LetterType.STANDARD;
							
							List<LetterData> toSend = new ArrayList<>();
							
							for(PlayerInfo pi : this.getRecipients().getPlayerList() ) {
								Data data = new DataFactory(pi.getUuid(), player.getName(), this.getObject().toString() );
								toSend.add(new LetterDataFactory(data, type, Arrays.asList(new String[] {this.getContent().toString()}), false) );
								
							}
							
							MailBoxController.sendLetters(player, toSend);
							
							player.sendMessage(LangManager.getValue("send_item_notification", ": " + this.getRecipients().getPreviewString()) );
							player.closeInventory();
							return;
							
							
						}
					}));

		}

	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		initializeInventory(player, contents);
	}

	public IdentifiersList getRecipients() {
		return recipients;
	}

	public void setRecipients(IdentifiersList recipients) {
		this.recipients = recipients;
	}

	public StringBuilder getObject() {
		return object;
	}

	public StringBuilder getContent() {
		return content;
	}

}