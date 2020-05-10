package fr.bletrazer.mailbox.inventory.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.DataManager;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.MailBoxInventoryHandler;
import fr.bletrazer.mailbox.inventory.MarkAllLettersInventory;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.creation.CreationInventory;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.bletrazer.mailbox.sql.SQLConnection;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class LetterInventory extends InventoryBuilder {
	private static final String TITLE = LangManager.getValue("string_menu_letters");

	// primary
	public static final String ID = "MailBox_Letters";

	private static final String PLAYER_FILTER = LangManager.getValue("string_player_filter");
	private static final String SHOW_SENDER = LangManager.getValue("string_show_sender");
	private static final String DELETE_FILTER = LangManager.getValue("help_delete_filter");
	private static final String QUESTION_CLEAN = LangManager.getValue("question_clean_letters");
	private static final String PERMISSION_NEEDED = LangManager.getValue("string_permission_needed");
	private static final String REPLY_ID = LangManager.getValue("string_reply_identifier");
	private static final String DELETE_LETTER = LangManager.getValue("question_delete_letter");
	private static final String MARK_ALL = LangManager.getValue("string_help_mark_all");
	private static final String NON_READ = LangManager.getValue("string_non_read_letters");
	private static final String DISPLAY = LangManager.getValue("string_display");
	private static final String DESCENDING = LangManager.getValue("string_descending_order");
	private static final String ASCENDING = LangManager.getValue("string_ascending_order");
	private static final String TOGGLE = LangManager.getValue("help_toggle_date_order");
	private static final String TYPE_FILTER = LangManager.getValue("string_filter_by_type");
	private static final String TYPE_FILTER_1 = LangManager.getValue("help_choose_type_filter_1");
	private static final String TYPE_FILTER_2 = LangManager.getValue("help_choose_type_filter_2");

	private DataHolder dataSource;

	// secondary
	private List<LetterData> toShow = new ArrayList<>();
	private IdentifiersList idList = new IdentifiersList(null);
	private Integer letterTypeIndex = 0;
	private Integer notReadYet = 0;
	private Boolean isSortingByDecreasingDate = true;

	// builder
	public LetterInventory(DataHolder dataSource) {
		super(ID, "§l" + TITLE, 5);
		this.setDataSource(dataSource);

	}

	public LetterInventory(DataHolder dataSource, InventoryBuilder parent) {
		super(ID, "§l" + TITLE, 5);
		this.setDataSource(dataSource);
		this.setParent(parent);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		
		// CONTENT
		this.dataContent(player, contents);

		contents.fillRow(3, ClickableItem.empty(new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build()));

		// FOOT
		if (!pagination.isFirst()) {
			contents.set(4, 1, this.previousPageItem(player, contents));
		}

		this.idFilterButton(player, contents);
		this.nonReadButton(player, contents);
		this.dateSortButton(player, contents);
		this.typeFilterButton(player, contents);

		this.DeleteAllButton(player, contents);

		if (!pagination.isLast()) {
			contents.set(4, 7, this.nextPageItem(player, contents));
		}
	}

	private void applyFilters(Player player, InventoryContents contents) {
		this.setToShow(DataManager.getTypeData(this.getDataSource(), LetterData.class));
		
		if (!this.getIdList().isEmpty()) {
			this.setToShow(this.filterByAuthors(this.getToShow()));

		}
		if (LetterType.values()[this.letterTypeIndex] != LetterType.NO_TYPE) {
			this.setToShow(getToShow().stream().filter(letter -> letter.getLetterType() == LetterType.values()[this.letterTypeIndex]).collect(Collectors.toList()) );

		}
		if (!this.getIsSortingByDecreasingDate()) {
			this.getToShow().sort(DataManager.descendingDateComparator().reversed());

		}
		
		this.nonReadButton(player, contents);
		this.DeleteAllButton(player, contents);
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		int state = contents.property("state", 0);
		contents.setProperty("state", state + 1);

		if (state % 20 != 0) {
			return;
		}

		this.dataContent(player, contents);
	}

	private void dataContent(Player player, InventoryContents contents) {
		applyFilters(player, contents);
		ClickableItem[] clickableItems = new ClickableItem[toShow.size()];

		for (Integer index = 0; index < getToShow().size(); index++) {
			LetterData tempData = getToShow().get(index);

			clickableItems[index] = ClickableItem.of(MailBoxInventoryHandler.generateItemRepresentation(tempData), e -> {
				ClickType clickType = e.getClick();

				if (clickType == ClickType.LEFT) {// lire
					if (player.getUniqueId().equals(tempData.getOwnerUuid()) && player.hasPermission("mailbox.letter.read.self") || player.hasPermission("mailbox.letter.read.other")) {
						MailBoxController.readLetter(player, tempData);

					} else {
						MessageUtils.sendMessage(player, MessageLevel.ERROR, PERMISSION_NEEDED);
					}

				} else if (clickType == ClickType.RIGHT) {// répondre
					if (player.getUniqueId().equals(tempData.getOwnerUuid()) && player.hasPermission("mailbox.letter.reply.self") || player.hasPermission("mailbox.letter.reply.other")) {
						CreationInventory ci = CreationInventory.newInventory(player.getUniqueId());
						IdentifiersList tempIdList = new IdentifiersList(null);
						tempIdList.addIdentifier(tempData.getAuthor());
						ci.setRecipients(tempIdList);
						String tempObj = tempData.getObject().replace(REPLY_ID + ": ", "");
						ci.setObject(REPLY_ID + ": " + tempObj);
						ci.setParent(this);

						ci.openInventory(player);

					} else {
						MessageUtils.sendMessage(player, MessageLevel.ERROR, PERMISSION_NEEDED);
					}

				} else if (clickType == ClickType.CONTROL_DROP || clickType == ClickType.DROP) {// supprimer
					if (player.getUniqueId().equals(tempData.getOwnerUuid()) && player.hasPermission("mailbox.letter.delete.self") || player.hasPermission("mailbox.letter.delete.other")) {
						DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), tempData, "§4§l" + DELETE_LETTER, this);
						inv.openInventory(player);

					} else {
						MessageUtils.sendMessage(player, MessageLevel.ERROR, PERMISSION_NEEDED);
					}
				}

			});
		}

		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
	}

	private List<LetterData> filterByAuthors(List<LetterData> list) {
		List<String> authorsNames = this.getIdList().getPlayerList().stream().map(PlayerInfo::getName).collect(Collectors.toList());

		List<LetterData> res = list.stream().filter(letterData -> authorsNames.contains(letterData.getAuthor())).collect(Collectors.toList());

		return res;
	};

	public static List<LetterData> filterByReadState(List<LetterData> letterList, Boolean isRead) {
		List<LetterData> res = letterList.stream().filter(letter -> letter.getIsRead().equals(isRead)).collect(Collectors.toList());

		return res;
	};

	// generate items
	private void idFilterButton(Player player, InventoryContents contents) {
		contents.set(4, 2, ClickableItem
				.of(new ItemStackBuilder(Material.PLAYER_HEAD).setName("§e§l" + PLAYER_FILTER + ":").addLores(this.getIdList().getPreviewLore()).addAutoFormatingLore(DELETE_FILTER, 35).build(), e -> {
					ClickType click = e.getClick();

					if (click == ClickType.LEFT) {
						PlayerSelectionInventory selector = new PlayerSelectionInventory(this.getIdList(), "§l" + SHOW_SENDER + ":", this);
						selector.openInventory(player);

					} else if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
						this.getIdList().clear();
						this.dataContent(player, contents);
						this.idFilterButton(player, contents);
					}
				}));
	}
	
	private void DeleteAllButton(Player player, InventoryContents contents) {
		if (this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.letter.delete.self") || player.hasPermission("mailbox.letter.delete.other")) {
			contents.set(4, 4, ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§c§lSupprimer les lettres affichées.").build(), e -> {
				if (this.getToShow().size() > 0) {
					if (e.getClick() == ClickType.LEFT) {
						if (SQLConnection.getInstance().isConnected()) {
							DeletionDatasInventory inv = new DeletionDatasInventory(this.dataSource, getToShow().stream().collect(Collectors.toList()),
									"§4§l" + LangManager.format(QUESTION_CLEAN, getToShow().size()), this, false);
							inv.openInventory(player);

						} else {
							MessageUtils.sendMessage(player, MessageLevel.ERROR, LangManager.getValue("string_error_player"));
							player.closeInventory();
							return;
						}
					}
				}
			}));
		}
	}
	
	private void nonReadButton(Player player, InventoryContents contents) {
		List<LetterData> list = filterByReadState(DataManager.getTypeData(this.getDataSource(), LetterData.class), false);

		ItemStack itemStack = new ItemStackBuilder(Material.BELL).setName("§e§l" + LangManager.format(NON_READ, list.size())).setAutoFormatingLore(MARK_ALL, 23).setStackSize(list.size(), false).build();

		Consumer<InventoryClickEvent> consumer = null;

		if (!list.isEmpty() ) {
			consumer = e -> {

				if (player.getUniqueId().equals(this.getDataSource().getOwnerUuid()) && player.hasPermission("mailbox.letter.markall.self") || player.hasPermission("mailbox.letter.markall.other")) {
					MarkAllLettersInventory inv = new MarkAllLettersInventory(list, this);
					inv.openInventory(player);

				}
			};

		}

		ClickableItem res;

		if (consumer != null) {
			res = ClickableItem.of(itemStack, consumer);

		} else {
			res = ClickableItem.empty(itemStack);
		}

		contents.set(4, 6, res);
	}

	private void dateSortButton(Player player, InventoryContents contents) {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(Material.REPEATER).setName("§e§l" + DISPLAY + ":");

		if (this.getIsSortingByDecreasingDate()) {
			itemStackBuilder.addLore(DESCENDING);

		} else {
			itemStackBuilder.addLore(ASCENDING);

		}

		itemStackBuilder.addLore(TOGGLE);

		contents.set(4,  5, ClickableItem.of(itemStackBuilder.build(), e -> {
			if (e.getClick() == ClickType.LEFT) {
				this.setIsSortingByDecreasingDate(!this.getIsSortingByDecreasingDate());
				dataContent(player, contents);
				dateSortButton(player, contents);
				
			}
		}));

	}

	private void typeFilterButton(Player player, InventoryContents contents) {
		LetterType type = LetterType.values()[this.letterTypeIndex];

		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(type.getMaterial()).setName("§e§l" + TYPE_FILTER + ": ")
				.addLore(type.getTranslation())
				.addLore(TYPE_FILTER_1).addLore(TYPE_FILTER_2)
				.addLore(DELETE_FILTER);

		contents.set(4, 3, ClickableItem.of(itemStackBuilder.build(), e -> {
			ClickType click = e.getClick();
			Boolean b = false;

			if (click == ClickType.RIGHT) {
				this.cycleIndex("+");
				b = true;

			} else if (click == ClickType.LEFT) {
				this.cycleIndex("-");
				b = true;

			} else if (click == ClickType.DROP) {
				this.setLetterTypeIndex(0);
				b = true;
			}

			if (b) {
				dataContent(player, contents);
				typeFilterButton(player, contents);
			}

		}));

	}

	// manipulation
	private void cycleIndex(String c) {
		Integer index = this.letterTypeIndex;
		Integer minIndex = 0;
		Integer maxIndex = LetterType.values().length - 1;

		if (c.equals("+")) {
			index = index + 1;

		} else if (c.equals("-")) {
			index = index - 1;
		}

		if (index < minIndex) {
			index = maxIndex;

		} else if (index > maxIndex) {
			index = minIndex;

		}

		this.setLetterTypeIndex(index);

	}

	// getters setters
	public void setLetterTypeIndex(Integer letterTypeIndex) {
		this.letterTypeIndex = letterTypeIndex;
	}

	public DataHolder getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataHolder dataSource) {
		this.dataSource = dataSource;
	}

	public Boolean getIsSortingByDecreasingDate() {
		return isSortingByDecreasingDate;
	}

	public void setIsSortingByDecreasingDate(Boolean isSortingByDecreasingDate) {
		this.isSortingByDecreasingDate = isSortingByDecreasingDate;
	}

	public List<LetterData> getToShow() {
		return toShow;
	}

	public void setToShow(List<LetterData> toShow) {
		this.toShow = toShow;
	}

	public Integer getNotReadYet() {
		return notReadYet;
	}

	public void setNotReadYet(Integer notReadYet) {
		this.notReadYet = notReadYet;
	}

	public IdentifiersList getIdList() {
		return idList;
	}

	public void setIdList(IdentifiersList idList) {
		this.idList = idList;
	}
}
