package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiableAuthors;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.PlayerChatSelector;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class PlayerSelectorInventory extends InventoryBuilder {
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	
	private IdentifiableAuthors identifiableAuthors;
	private PlayerChatSelector selector = null;
	
	public PlayerSelectorInventory(IdentifiableAuthors identifiableAuthors, String invTitle ) {
		super("MailBox_Player_Selector", invTitle, 3);
		this.setAuthorFilter(identifiableAuthors);
	}
	
	public PlayerSelectorInventory(IdentifiableAuthors identifiableAuthors, String invTitle, InventoryBuilder parent) {
		super("MailBox_Player_Selector", invTitle, 3);
		super.setParent(parent);
		this.setAuthorFilter(identifiableAuthors);
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		if(this.getSelector() == null) {
			this.setSelector(new PlayerChatSelector(this.getAuthorFilter(), this));
			
		}
		
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_faction")).build(), e -> {
			
		}));
		
		contents.set(1, 4, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_precise_player")).build(), e -> {
			if(!PlayerChatSelector.isUsingPCS(player) ) {
				this.setFinalClose(false);
				this.getSelector().start(player);
				player.closeInventory();
			}
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_server")).build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				this.getAuthorFilter().addIdentifier("#online" );
				
			} else if (clickType == ClickType.RIGHT ) {
				this.getAuthorFilter().addIdentifier("#offline");
			}
			
		}));
		
		if(this.getParent() != null) {
			contents.set(2, 0, this.goBackItem(player) );
		}
		
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(Material.REDSTONE)
				.setName("§f§l"+LangManager.getValue("string_displayed_players")+":")
				.addLores(this.getAuthorFilter().getPreviewLore())
				.addLore(LangManager.getValue("help_delete_player_filter"))
				.build(), e -> {
					if(e.getClick() == ClickType.RIGHT) {
						this.getAuthorFilter().reset();
					}
				}));
		

	}

	public PlayerChatSelector getSelector() {
		return selector;
	}

	public void setSelector(PlayerChatSelector selector) {
		this.selector = selector;
	}

	public IdentifiableAuthors getAuthorFilter() {
		return identifiableAuthors;
	}
	public void setAuthorFilter(IdentifiableAuthors identifiableAuthors) {
		this.identifiableAuthors = identifiableAuthors;
	}
	
}
