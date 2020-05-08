package fr.bletrazer.mailbox.inventory.inventories.creation;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.PlayerSelectionInventory;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Duration;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Player;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_SimpleMessage;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class CreationInventory extends InventoryBuilder {

	public static final String ID = "MailBox_creation";

	private static final String DEFAULT_OBJECT = LangManager.getValue("string_no_object");
	private static final String INVENTORY_NAME = LangManager.getValue("string_menu_creation");
	private static final String RECIPIENTS = LangManager.getValue("string_recipients");
	private static final String TARGET_SELECTION = LangManager.getValue("string_target_selection");
	private static final String OBJECT = LangManager.getValue("string_object");
	private static final String CH_OBJECT_START = LangManager.getValue("string_ch_object_start");
	private static final String MESSAGE = LangManager.getValue("string_message");
	private static final String MESSAGE_EMPTY = LangManager.getValue("string_no_message");
	private static final String CH_MESSAGE_START = LangManager.getValue("string_ch_letter_content_start");
	private static final String ITEM = LangManager.getValue("string_item");
	private static final String RESET = LangManager.getValue("string_reset_creation");
	private static final String DELETE_ITEM = LangManager.getValue("string_delete_creation_item");
	private static final String ADD_ITEMSTACK = LangManager.getValue("string_drop_item_to_configure");
	private static final String SEND = LangManager.getValue("string_click_to_send");
	private static final String DURATION = LangManager.getValue("string_duration");

	private static final String DEFAULT_DURATION_STR = "infini";

	private static Map<UUID, CreationInventory> map = new HashMap<>();

	private IdentifiersList recipients;
	private StringBuilder object = new StringBuilder(DEFAULT_OBJECT);
	private StringBuilder content = new StringBuilder();
	private ItemStack item;
	private StringBuilder strDuration = new StringBuilder(DEFAULT_DURATION_STR);

	public CreationInventory() {
		super(ID, "§l" + INVENTORY_NAME, 4);

	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		if (this.getRecipients() == null) {
			this.setRecipients(new IdentifiersList(player.getName()));
		}

		this.objectButton(player, contents);
		this.messageButton(player, contents);
		this.itemStackButton(player, contents);
		if (this.getItem() != null) {
			this.clockButton(player, contents);
		}
		this.targetsButton(player, contents);
		this.sendButton(player, contents);
		this.resetButton(player, contents);

	}

	private void targetsButton(Player player, InventoryContents contents) {
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("§e§l" + RECIPIENTS + ":").setLore(this.getRecipients().getPreviewLore()).build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.LEFT) {
				if (player.hasPermission("mailbox.send.announce")) {
					PlayerSelectionInventory selector = new PlayerSelectionInventory(this.getRecipients(), "§l" + TARGET_SELECTION + ":", this);
					selector.setFilterMode(false);
					selector.openInventory(player);

				} else {
					ChatHooker chCheck = new CH_Player(this.getRecipients(), this);
					player.closeInventory();
					chCheck.start(player);
				}
			} else if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
				this.getRecipients().clear();
				this.targetsButton(player, contents);
			}

		}));
	}

	private void objectButton(Player player, InventoryContents contents) {
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(Material.WHITE_BANNER).setName("§e§l" + OBJECT + ":").addAutoFormatingLore(this.getObject().toString(), 35).build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.LEFT) {
				ChatHooker chCheck = ChatHooker.get(player.getUniqueId());

				if (chCheck == null) {
					player.closeInventory();
					CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterObject_ChatHooker", CH_OBJECT_START, this.getObject(), this);
					ch.start(player);

				} else {
					// TODO message au joueur ? "veuillez d'abord terminer votre entrée précédente"
				}

			} else if (click == ClickType.CONTROL_DROP || click == ClickType.DROP) {
				this.setObject(DEFAULT_OBJECT);
				objectButton(player, contents);
			}
		}));
	}

	private void messageButton(Player player, InventoryContents contents) {
		contents.set(1, 3, ClickableItem.of(new ItemStackBuilder(Material.WRITABLE_BOOK).setName("§e§l" + MESSAGE + ":")
				.addAutoFormatingLore(this.getContent().toString().isEmpty() ? MESSAGE_EMPTY : this.getContent().toString(), 35).build(), e -> {
					ClickType click = e.getClick();

					if (click == ClickType.LEFT) {
						ChatHooker chCheck = ChatHooker.get(player.getUniqueId());

						if (chCheck == null) {
							player.closeInventory();
							CH_SimpleMessage ch = new CH_SimpleMessage("MailBox_LetterContent_ChatHooker", CH_MESSAGE_START, this.getContent(), this);
							ch.start(player);

						} else {
							// TODO message au joueur ? "veuillez d'abord terminer votre entrée précédente"
						}
					} else if (click == ClickType.CONTROL_DROP || click == ClickType.DROP) {
						this.setMessage(new StringBuilder());
						messageButton(player, contents);
					}
				}));
	}

	private void resetButton(Player player, InventoryContents contents) {
		contents.set(3, 4, ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§c§l" + RESET).build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.LEFT) {
				ResetConfirmationInventory resetInv = new ResetConfirmationInventory(this);
				resetInv.openInventory(player);
			}
		}));
	}

	private void itemStackButton(Player player, InventoryContents contents) {
		if (player.hasPermission("mailbox.item.send")) {
			ItemStack toShow = this.getItem();

			if (toShow == null) {
				toShow = new ItemStackBuilder(this.getItem() == null ? Material.ITEM_FRAME : this.getItem().getType()).setName("§e§l" + ITEM).addAutoFormatingLore(ADD_ITEMSTACK, 35)
						.addAutoFormatingLore(DELETE_ITEM, 35).build();
			}

			contents.set(1, 4, ClickableItem.of(toShow, e -> {
				ClickType click = e.getClick();
				ItemStack cursor = e.getCursor();

				if (click == ClickType.LEFT) {
					if (cursor != null && cursor.getType() != Material.AIR) {
						this.setItem(cursor.clone());
						clockButton(player, contents);
						itemStackButton(player, contents);

					}

				} else if (click == ClickType.CONTROL_DROP || click == ClickType.DROP) {
					this.setItem(null);
					contents.set(1, 5, null);
					this.setStrDuration(new StringBuilder(DEFAULT_DURATION_STR));
					itemStackButton(player, contents);
				}

			}));
		}
	}

	private void clockButton(Player player, InventoryContents contents) {
		contents.set(1, 5, ClickableItem.of(new ItemStackBuilder(Material.CLOCK).setName("§e§l" + DURATION).addLore(getStrDuration().toString()).build(), e -> {
			ClickType click = e.getClick();
			ChatHooker chCheck = ChatHooker.get(player.getUniqueId());

			if (click == ClickType.LEFT) {
				if (chCheck == null) {
					player.closeInventory();
					CH_Duration ch_duration = new CH_Duration(this.getStrDuration(), this);
					ch_duration.start(player);
				}
			} else if (click == ClickType.CONTROL_DROP || click == ClickType.DROP) {
				setStrDuration(new StringBuilder(DEFAULT_DURATION_STR));
				clockButton(player, contents);
			}
		}));
	}

	private void sendButton(Player player, InventoryContents contents) {
		contents.set(3, 8, ClickableItem.of(new ItemStackBuilder(Material.FEATHER).setName("§e§l" + SEND).build(), e -> {

			if (!this.getRecipients().isEmpty()) {
				if (!this.getContent().toString().isEmpty() || this.getItem() != null) {
					ClickType click = e.getClick();

					if (click == ClickType.LEFT) {
						SendConfirmationInventory sendInv = new SendConfirmationInventory(this, this.getRecipients(), this.getObject().toString(), this.getContent().toString(),
								getStrDuration().toString().equals(DEFAULT_DURATION_STR) ? Duration.ofSeconds(0) : CH_Duration.transform(getStrDuration().toString()), this.getItem());
						sendInv.openInventory(player);

					}
				}
			}
		}));

	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
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

	public void setMessage(StringBuilder sb) {
		this.content = sb;
	}

	public void setStrDuration(StringBuilder strDuration) {
		this.strDuration = strDuration;
	}

	public static CreationInventory getInventory(UUID uuid) {
		CreationInventory res = map.get(uuid);

		if (res == null) {
			res = newInventory(uuid);
		}

		return res;
	}

	public static CreationInventory newInventory(UUID uuid) {
		CreationInventory temp = new CreationInventory();
		map.put(uuid, temp);
		return temp;
	}

}