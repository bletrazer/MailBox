package fr.bletrazer.mailbox.inventory.inventories;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;
import fr.bletrazer.mailbox.DataManager.factories.LetterDataFactory;
import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Duration;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Player;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_SimpleMessage;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class CreationInventory extends InventoryBuilder {

	public static final String ID = "MailBox_creation";

	private IdentifiersList recipients;
	private StringBuilder object = new StringBuilder(LangManager.getValue("string_no_object"));
	private StringBuilder content = new StringBuilder();
	private ItemStack item;
	private StringBuilder strDuration = new StringBuilder("infini");
	
	private Boolean showClock = false;
	
	public CreationInventory() {
		super(ID, "§l" + LangManager.getValue("string_menu_creation"), 3);

	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		if (this.getRecipients() == null) {
			this.setRecipients(new IdentifiersList(player.getName()));
		}
		
		if(this.showClock ) {
			this.setClock(player, contents);
		}
		
		this.setPlayerFilter(player, contents);

		contents.set(2, 0, this.goBackItem(player));

	}
	
	private void setPlayerFilter(Player player, InventoryContents contents) {
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("§e§l" + LangManager.getValue("string_recipients")+ ":")
				.setLore(this.getRecipients().getPreviewLore()).build(), e -> {
					ClickType click = e.getClick();
					
					if (click == ClickType.LEFT) {
							if(player.hasPermission("mailbox.send.announce")) {
								PlayerSelectionInventory selector = new PlayerSelectionInventory(this.getRecipients(),
										"§l" + LangManager.getValue("string_target_selection") + ":", this);
								selector.setFilterMode(false);
								selector.openInventory(player);
								
							} else {
								ChatHooker chCheck = new CH_Player(this.getRecipients(), this);
								player.closeInventory();
								chCheck.start(player);
							}
					} else if (click == ClickType.DROP ) {
						this.getRecipients().clear();
						this.setPlayerFilter(player, contents);
					}
					
					
				}));
	}
	
	private void dynamicContent(Player player, InventoryContents contents) {
		
		contents.set(1, 1,
				ClickableItem.of(new ItemStackBuilder(Material.WHITE_BANNER).setName("§e§l" + LangManager.getValue("string_object")+ ":" )
						.addAutoFormatingLore(this.getObject().toString(), 35)
						.build(), e -> {
							ClickType click = e.getClick();
							
							if (click == ClickType.LEFT) {
								ChatHooker chCheck = ChatHooker.get(player.getUniqueId());
								
								if (chCheck == null) {
									player.closeInventory();
									CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterObject_ChatHooker", LangManager.getValue("string_ch_object_start"), this.getObject(), this);
									ch.start(player);
								}
							}
						}));
		
		contents.set(1, 2,
				ClickableItem.of(new ItemStackBuilder(Material.WRITABLE_BOOK).setName("§e§l" + LangManager.getValue("string_message") + ":")
						.addAutoFormatingLore(this.getContent().toString().isEmpty() ? LangManager.getValue("string_no_message") : this.getContent().toString(), 35)
						.build(), e -> {
							ClickType click = e.getClick();
							
							if (click == ClickType.LEFT) {
								ChatHooker chCheck = ChatHooker.get(player.getUniqueId());
								
								if (chCheck == null) {
									player.closeInventory();
									CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterContent_ChatHooker", LangManager.getValue("string_ch_letter_content_start"), this.getContent(), this);
									ch.start(player);

								}
							}
						}));
		
		if(player.hasPermission("mailbox.send.items")) {
			ItemStack toShow = this.getItem();
			
			if(toShow == null) {
				toShow = new ItemStackBuilder(this.getItem() == null ? Material.ITEM_FRAME : this.getItem().getType()).setName("§e§l" + LangManager.getValue("string_item"))
						.addAutoFormatingLore(LangManager.getValue("string_drop_item_to_configure"), 35)
						.addAutoFormatingLore(LangManager.getValue("string_delete_creation_item"), 35)
						.build();
			}
			
			contents.set(1, 3, ClickableItem.of(toShow, e -> {
								ClickType click = e.getClick();
								ItemStack cursor = e.getCursor();
								
								if (click == ClickType.LEFT) {
									if(cursor != null && cursor.getType() != Material.AIR ) {
										this.setItem(cursor.clone());
										setClock(player, contents);
										this.showClock = true;
										
									}
									
								} else if (click == ClickType.CONTROL_DROP || click == ClickType.DROP ) {
									this.setItem(null);
									removeClock(contents);
									this.setStrDuration(new StringBuilder("infini") );
									this.showClock = false;
								}
	
							}));
		}
		
		if (!this.getRecipients().isEmpty() ) {
			if(!this.getContent().toString().isEmpty() || this.getItem() != null) {
			
				contents.set(1, 7, ClickableItem.of(new ItemStackBuilder(Material.FEATHER).setName("§e§l" + LangManager.getValue("string_click_to_send")).build(), e -> {
							ClickType click = e.getClick();
	
							if (click == ClickType.LEFT) {
								ConfirmationInventoryBuilder confInv = new ConfirmationInventoryBuilder("confirmation_sendLetter", "§l" + LangManager.getValue("string_send_confirmation") ) {
	
									@Override
									public void onUpdate(Player player, InventoryContents contents) {
									}
	
									@Override
									public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
										return e -> {
											ClickType click = e.getClick();
											
											if(click == ClickType.LEFT ) {
												LetterType type = getRecipients().getPlayerList().size() > 1 ? LetterType.ANNOUNCE : LetterType.STANDARD;
												List<LetterData> letters = new ArrayList<>();
												List<ItemData> items = new ArrayList<>();
												
												for (PlayerInfo pi : getRecipients().getPlayerList() ) {
													Data data = new DataFactory(pi.getUuid(), player.getName(), getObject().toString());
													
													if(!getContent().toString().isEmpty() ) {
														letters.add(new LetterDataFactory(data.clone(), type, Arrays.asList(new String[] { getContent().toString() }), false));
													}
													
													if(getItem() != null) {
														Duration tempDuration = getStrDuration().toString().equals("infini") ? Duration.ofSeconds(0) : CH_Duration.transform(getStrDuration().toString());
														items.add(new ItemData(data.clone(), getItem(), tempDuration) );
													}
													
												}
												
												if(!letters.isEmpty() ) {
													if(MailBoxController.sendLetters(letters) ) {
														MessageUtils.sendMessage(player, MessageLevel.INFO, LangManager.getValue("string_send_letter", ": " + getRecipients().getPreviewString()) );
													}
												}
												
												if(!items.isEmpty() ) {
													if(MailBoxController.sendItems(items) ) {
														MessageUtils.sendMessage(player, MessageLevel.INFO, LangManager.getValue("string_send_item", ": " + getRecipients().getPreviewString()) );
													}
												}
												
												player.closeInventory();
		
											}
										};
									}
	
									@Override
									public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
										return null;
									}
								};
								
								confInv.setParent(this);
								confInv.openInventory(player);
	
							}
						}));
	
			} else {
				contents.set(1, 7, null);
			}
		} else {
			contents.set(1, 7, null);
		}
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}
	
	private void setClock(Player player, InventoryContents contents) {
		contents.set(0, 3, ClickableItem.of(new ItemStackBuilder(Material.CLOCK).setName("§e§l" + LangManager.getValue("string_duration"))
				.addLore(getStrDuration().toString() )
				.build(), e -> {
					ClickType click = e.getClick();
					ChatHooker chCheck = ChatHooker.get(player.getUniqueId());
					
					if (click == ClickType.LEFT) {
						if (chCheck == null) {
							player.closeInventory();
							CH_Duration ch_duration = new CH_Duration(this.getStrDuration(), this);
							ch_duration.start(player);
						}
					}
				}));
	}
	
	private void removeClock(InventoryContents contents) {
		contents.set(0, 3, null);
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

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public StringBuilder getStrDuration() {
		return strDuration;
	}
	
	public void setObject(String str) {
		this.object = new StringBuilder(str);
	}

	public void setStrDuration(StringBuilder strDuration) {
		this.strDuration = strDuration;
	}

}