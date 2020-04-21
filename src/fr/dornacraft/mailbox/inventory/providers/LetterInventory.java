package fr.dornacraft.mailbox.inventory.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.DataHolder;
import fr.dornacraft.mailbox.DataManager.DataManager;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;
import fr.dornacraft.mailbox.DataManager.MailBoxController;
import fr.dornacraft.mailbox.inventory.MailBoxInventoryHandler;
import fr.dornacraft.mailbox.inventory.builders.InventoryProviderBuilder;
import fr.dornacraft.mailbox.inventory.providers.utils.IdentifiableAuthors;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.sql.LetterDataSQL;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class LetterInventory extends InventoryProviderBuilder {

	// Materials
	public static Material NON_READ_LETTERS_MATERIAL = Material.BELL;
	public static Material PLAYER_FILTER_MATERIAL = Material.PLAYER_HEAD;
	public static Material DATE_SORT_MATERIAL = Material.REPEATER;

	// primary
	private DataHolder dataSource;

	// secondary
	private List<LetterData> toShow = new ArrayList<>();
	private IdentifiableAuthors filter = new IdentifiableAuthors();
	private LetterType showedLetterType = null;
	private Integer letterTypeIndex = -1;
	private Integer notReadYet = 0;
	private Boolean isSortingByDecreasingDate = true;

	// builder
	public LetterInventory(DataHolder dataSource) {
		super("MailBox_Letters", "§lMenu des lettres", 5);

		this.setDataSource(dataSource);

	}

	public LetterInventory(DataHolder dataSource, InventoryProviderBuilder parent) {
		super("MailBox_Letters", "§lMenu des lettres", 5);

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

		contents.set(4, 2,ClickableItem.of(new ItemStackBuilder(PLAYER_FILTER_MATERIAL)
				.setName("§c§7Filtre joueur:" ).setLore(this.getAuthorFilter().getPreview()).build(), e -> {
							PlayerSelectorInventory selector = new PlayerSelectorInventory(this.getAuthorFilter(), "§lExpéditeurs a affichés:", this);
							selector.openInventory(player);

						}));

		contents.set(4, 4, ClickableItem.of(new ItemStackBuilder(MailBoxInventoryHandler.DELETE_ALL_MATERIAL)
				.setName("§4§lSupprimer les lettres affichées.").build(), e -> {
			        List<Long> idList = toShow.stream()
			                .map(LetterData::getId)
			                .collect(Collectors.toList());

					DeletionDatasInventory inv = new DeletionDatasInventory(this.dataSource, idList, "§4§lSupprimer les " + idList.size() + " lettres ?", this);
					inv.openInventory(player);
					
				}));

		if (!pagination.isLast()) {
			contents.set(4, 7, this.nextPageItem(player, contents));
		}

		contents.set(4, 0, this.goBackItem(player));
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		this.dynamicContent(player, contents);
	}

	private void dynamicContent(Player player, InventoryContents contents) {
		this.setToShow(DataManager.getTypeData(this.getDataSource(), LetterData.class) );
		
		if (this.getShowedLetterType() != null) {
			this.setToShow(this.filterByType(this.getToShow()) );

		}

		if (!this.getAuthorFilter().getPlayerList().isEmpty()) {
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

					} else {// lecture dans le chat
						MailBoxController.readLetter(player, tempData);

					}

				} else if (clickType == ClickType.RIGHT && player.getUniqueId().equals(tempData.getUuid())) {//Toggle read state
					if(tempData.getUuid().equals(player.getUniqueId()) ){
						tempData.setIsRead(!tempData.getIsRead());
						LetterDataSQL.getInstance().update(tempData);
					}

				} else if (clickType == ClickType.CONTROL_DROP) {// supprimer
					DeletionDataInventory inv = new DeletionDataInventory(this.getDataSource(), tempData.getId(), "§c§lSupprimer la lettre ?", this);
					inv.openInventory(player);

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
		List<String> authorsNames = this.getAuthorFilter().getPlayerList().stream()
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

	// generate items
	private ClickableItem generateNonReadLettersItem(Player player) {
		List<LetterData> list = filterByReadState(DataManager.getTypeData(this.getDataSource(), LetterData.class), false);
		
		ItemStack itemStack = new ItemStackBuilder(NON_READ_LETTERS_MATERIAL)
				.setName(String.format("§l§e%s lettres non lues.", list.size() ))
				.addLore("clique pour toutes les")
				.addLore("marquée comme lues.")
				.setStackSize(list.size(), false )
				.build();

		return ClickableItem.of(itemStack, e -> {
			for (LetterData letterData : list ) {
				if(letterData.getUuid().equals(player.getUniqueId()) ){
					letterData.setIsRead(true);
					LetterDataSQL.getInstance().update(letterData);
				} else {
					break;
				}
			}
		});
	}

	private ClickableItem generateSortByDateItem() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(DATE_SORT_MATERIAL).setName("§7§lAffichage:");

		if (this.getIsSortingByDecreasingDate()) {

			itemStackBuilder.addLore(" -> du plus récent au plus ancien")
					.addLore("clique pour toutes les triées")
					.addLore("du plus ancien au plus récent.");
		} else {
			itemStackBuilder.addLore(" -> du plus ancien au plus récent")
					.addLore("clique pour toutes les triées")
					.addLore("du plus récent au plus ancien.");

		}

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			this.setIsSortingByDecreasingDate(!this.getIsSortingByDecreasingDate());
		});

	}

	private ClickableItem generateCycleFilters() {
		ItemStackBuilder itemStackBuilder = new ItemStackBuilder(this.getShowedLetterType().getMaterial())
						.addLore("droit / gauche: choisir filtre")
						.addLore("Drop pour supprimer le filtre");

		if (this.letterTypeIndex < 0) {
			this.setShowedLetterType(null);

		} else {
			this.setShowedLetterType(LetterType.values()[this.letterTypeIndex]);
		}

		String filter = this.getShowedLetterType() == null ? "aucun" : this.getShowedLetterType().name().toLowerCase();
		itemStackBuilder.setName("§f§lFiltre par type: " + filter);

		return ClickableItem.of(itemStackBuilder.build(), e -> {
			ClickType click = e.getClick();

			if (click == ClickType.RIGHT) {
				this.cycleAddIndex(true);

			} else if (click == ClickType.LEFT) {
				this.cycleAddIndex(false);

			} else if (click == ClickType.DROP) {
				this.setLetterTypeIndex(-1);
			}
		});

	}
	
	// manipulation
	private void cycleAddIndex(Boolean b) {
		Integer index = this.letterTypeIndex;
		Integer minIndex = -1;
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

	public IdentifiableAuthors getAuthorFilter() {
		return filter;
	}

	public void setAuthorFilter(IdentifiableAuthors filter) {
		this.filter = filter;
	}

	public Integer getNotReadYet() {
		return notReadYet;
	}

	public void setNotReadYet(Integer notReadYet) {
		this.notReadYet = notReadYet;
	}
}
