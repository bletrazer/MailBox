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
import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.bletrazer.mailbox.sql.LetterDataSQL;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class LetterInventory extends InventoryBuilder {

	// Materials
	public static Material NON_READ_LETTERS_MATERIAL = Material.BELL;
	public static Material PLAYER_FILTER_MATERIAL = Material.PLAYER_HEAD;
	public static Material DATE_SORT_MATERIAL = Material.REPEATER;

	// primary
	public static final String ID = "MailBox_Letters";
		
	private DataHolder dataSource;

	// secondary
	private List<LetterData> toShow = new ArrayList<>();
	private IdentifiersList idList = new IdentifiersList(null);
	private LetterType showedLetterType = LetterType.NO_TYPE;
	private Integer letterTypeIndex = 0;
	private Integer notReadYet = 0;
	private Boolean isSortingByDecreasingDate = true;

	// builder
	public LetterInventory(DataHolder dataSource) {
		super(ID, "§l"+LangManager.getValue("string_menu_letters"), 5);

		this.setDataSource(dataSource);

	}

	public LetterInventory(DataHolder dataSource, InventoryBuilder parent) {
		super(ID, "§l"+LangManager.getValue("string_menu_letters"), 5);

		this.setDataSource(dataSource);
		this.setParent(parent);
	}

	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);

		// CONTENT
		this.dynamicContent(player, contents);

		contents.fillRow(3,ClickableItem.empty(new ItemStackBuilder(MailBoxInventoryHandler.BORDER_MATERIAL).setName(" ").build()));

		// FOOT
		if (!pagination.isFirst()) {
			contents.set(4, 1, this.previousPageItem(player, contents));
		}
		
		this.setPlayerFilter(player, contents);
		
		if(this.getDataSource().getOwnerUuid().equals(player.getUniqueId()) && player.hasPermission("mailbox.delete.letter.self") || player.hasPermission("mailbox.delete.letter.other") ) {
			contents.set(4, 4, ClickableItem.of(new ItemStackBuilder(MailBoxInventoryHandler.DELETE_ALL_MATERIAL)
					.setName("§c§lSupprimer les lettres affichées.").build(), e -> {
				        List<Long> idList = getToShow().stream()
				                .map(LetterData::getId)
				                .collect(Collectors.toList());
	
						DeletionDatasInventory inv = new DeletionDatasInventory(this.dataSource, idList, "§4§l"+LangManager.getValue("question_clean_letters", idList.size()), this);
						inv.openInventory(player); 
						
					}));
			
		}

		if (!pagination.isLast()) {
			contents.set(4, 7, this.nextPageItem(player, contents));
		}

		contents.set(4, 0, this.goBackItem(player));
	}
	
	private void setPlayerFilter(Player player, InventoryContents contents) {
		contents.set(4, 2,ClickableItem.of(new ItemStackBuilder(PLAYER_FILTER_MATERIAL)
				.setName("§e§l"+LangManager.getValue("string_player_filter")+":" ).setLore(this.getIdList().getPreviewLore()).build(), e -> {
					ClickType click = e.getClick();

					if (click == ClickType.LEFT) {
						PlayerSelectionInventory selector = new PlayerSelectionInventory(this.getIdList(), "§l"+LangManager.getValue("string_show_sender")+":", this);
						selector.openInventory(player);

					} else if (click == ClickType.DROP) {
						this.getIdList().clear();
						this.setPlayerFilter(player, contents);
					}
				}));
	}
	
	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}

	private void dynamicContent(Player player, InventoryContents contents) {
		this.setToShow(DataManager.getTypeData(this.getDataSource(), LetterData.class) );
		
		if (this.getShowedLetterType() != LetterType.NO_TYPE) {
			this.setToShow(this.filterByType(this.getToShow()) );

		}

		if (!this.getIdList().isEmpty() ) {
			this.setToShow(this.filterByAuthors(this.getToShow()));
			
		}

		if (this.getIsSortingByDecreasingDate()) {
			this.getToShow().sort(DataManager.ascendingDateComparator().reversed());

		} else {
			this.getToShow().sort(DataManager.ascendingDateComparator());

		}
		
		ClickableItem[] clickableItems = new ClickableItem[toShow.size()];

		for (Integer index = 0; index < toShow.size(); index++) {
			LetterData tempData = toShow.get(index);

			clickableItems[index] = ClickableItem.of(MailBoxInventoryHandler.generateItemRepresentation(tempData), e -> {
				ClickType clickType = e.getClick();
				ItemStack cursor = e.getCursor();

				if (clickType == ClickType.LEFT) {
					if (cursor.getType() == Material.WRITTEN_BOOK) {
						System.out.println("OK");

					} else {// lecture dans un livre
						MailBoxController.readLetter(player, tempData);

					}

				} else if (clickType == ClickType.RIGHT && player.getUniqueId().equals(tempData.getOwnerUuid()) ) {//Toggle read state
					tempData.setIsRead(!tempData.getIsRead());
					LetterDataSQL.getInstance().update(tempData);

				} else if (clickType == ClickType.CONTROL_DROP) {// supprimer
					
					if(player.getUniqueId().equals(tempData.getOwnerUuid()) && player.hasPermission("mailbox.delete.letter.self") || player.hasPermission("mailbox.delete.letter.other") ) {
						DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), tempData.getId(), "§4§l"+LangManager.getValue("question_delete_letter"), this);
						inv.openInventory(player);
						
					} else {
						player.sendMessage(LangManager.getValue("string_permission_needed"));
					}
				}

			});
		}

		Pagination pagination = contents.pagination();
		pagination.setItems(clickableItems);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		contents.set(4, 3, generateCycleFilters());
		contents.set(4, 5, generateSortByDateItem());
		
		contents.set(4, 6, generateNonReadLettersItem(player));
	}

	private List<LetterData> filterByAuthors(List<LetterData> list) {
		List<String> authorsNames = this.getIdList().getPlayerList().stream()
				.map(PlayerInfo::getName)
				.collect(Collectors.toList());
		
        List<LetterData> res = list.stream()
                .filter(letterData -> authorsNames.contains(letterData.getAuthor() ) )
                .collect(Collectors.toList());   
		
		
		return res;
	};
	
	public static List<LetterData> filterByReadState(List<LetterData> letterList, Boolean isRead) {
        List<LetterData> res = letterList.stream()
                .filter(letter -> letter.getIsRead().equals(isRead) )
                .collect(Collectors.toList());
		
		return res;
	};
	
	private List<LetterData> filterByType(List<LetterData> letterList) {
        List<LetterData> res = letterList.stream()
                .filter(letter -> letter.getLetterType() == this.getShowedLetterType() )
                .collect(Collectors.toList());   
		
		return res;
	};
	
	private static final String help_mark_all = LangManager.getValue("help_mark_all");
	
	// generate items
	private ClickableItem generateNonReadLettersItem(Player player) {
		List<LetterData> list = filterByReadState(DataManager.getTypeData(this.getDataSource(), LetterData.class), false);
		
		ItemStack itemStack = new ItemStackBuilder(NON_READ_LETTERS_MATERIAL)
				.setName("§e§l" + LangManager.getValue("string_non_read_letters", list.size()) )
				.setAutoFormatingLore(help_mark_all, 23)
				.setStackSize(list.size(), false )
				.build();
		
		Consumer<InventoryClickEvent> consumer = null;
		
		if (getDataSource().getOwnerUuid().equals(player.getUniqueId()) ) {
			consumer = e -> {
				ConfirmationInventoryBuilder confInv = new ConfirmationInventoryBuilder("mark_all", "§l" + String.format("Vous allez marquer %s lettre commes lues", list.size())) {
					
					@Override
					public void onUpdate(Player player, InventoryContents contents) {
					}
					
					@Override
					public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
						return event -> {
							for (LetterData letterData : list) {
								letterData.setIsRead(true);
								LetterDataSQL.getInstance().update(letterData);
								
							}
							
							this.returnToParent(player);
							
						};
					}
					
					@Override
					public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
						return null;
					}
				};
				confInv.setParent(this);
				confInv.openInventory(player);
			};
			
		}
		
		ClickableItem res;
		
		if(consumer != null) {
			res = ClickableItem.of(itemStack, consumer);
			
		} else {
			res = ClickableItem.empty(itemStack);
		}
		
		return res;
	}

	private ClickableItem generateSortByDateItem() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(DATE_SORT_MATERIAL).setName("§e§l"+LangManager.getValue("string_display") + ":");

		if (this.getIsSortingByDecreasingDate()) {
			itemStackBuilder.addLore(LangManager.getValue("string_descending_order"));
			
		} else {
			itemStackBuilder.addLore(LangManager.getValue("string_ascending_order"));

		}
		
		itemStackBuilder.addLore(LangManager.getValue("help_toggle_date_order"));

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			this.setIsSortingByDecreasingDate(!this.getIsSortingByDecreasingDate());
		});

	}

	private ClickableItem generateCycleFilters() {
		String str = this.getShowedLetterType() == LetterType.NO_TYPE ? LangManager.getValue("string_no") : this.getShowedLetterType().name().toLowerCase();
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(this.getShowedLetterType().getMaterial())
						.setName("§e§l"+LangManager.getValue("string_filter_by_type")+": ")
						.addLore(str)
						.addLore(LangManager.getValue("help_choose_type_filter_1"))
						.addLore(LangManager.getValue("help_choose_type_filter_2"))
						.addLore(LangManager.getValue("help_delete_type_filter"));

		this.setShowedLetterType(LetterType.values()[this.letterTypeIndex]);

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.RIGHT) {
				this.cycleAddIndex(true);

			} else if (click == ClickType.LEFT) {
				this.cycleAddIndex(false);

			} else if (click == ClickType.DROP) {
				this.setLetterTypeIndex(0);
			}
		});

	}
	
	// manipulation
	private void cycleAddIndex(Boolean b) {
		Integer index = this.letterTypeIndex;
		Integer minIndex = 0;
		Integer maxIndex = LetterType.values().length - 1;

		if (b) {
			index = index + 1;

		} else {
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

	public LetterType getShowedLetterType() {
		return showedLetterType;
	}

	public void setShowedLetterType(LetterType showedLetterType) {
		this.showedLetterType = showedLetterType;
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
