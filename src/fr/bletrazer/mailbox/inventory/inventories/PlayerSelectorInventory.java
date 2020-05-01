package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Player;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class PlayerSelectorInventory extends InventoryBuilder {
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	
	private IdentifiersList identifiersList;
	
	public PlayerSelectorInventory(IdentifiersList identifiersList, String invTitle ) {
		super("MailBox_Player_Selector", invTitle, 3);
		this.setIdentifiersList(identifiersList);
	}
	
	public PlayerSelectorInventory(IdentifiersList identifiersList, String invTitle, InventoryBuilder parent) {
		super("MailBox_Player_Selector", invTitle, 3);
		super.setParent(parent);
		this.setIdentifiersList(identifiersList);
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		pagination.setItemsPerPage(27);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		/*
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_FACTION_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_faction")).build(), e -> {
			
		}));
		*/
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_precise_player")).build(), e -> {
			CH_Player ch_player = CH_Player.get(player.getUniqueId());
			
			if(ch_player == null) {
				ch_player = new CH_Player(this.getIdentifiersList(), this);
				ch_player.start(player);
				
			}
			
		}));
		
		contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL).setName("§f§l"+LangManager.getValue("string_choose_server")).build(), e -> {
			ClickType clickType = e.getClick();
			
			if(clickType == ClickType.LEFT ) {
				this.getIdentifiersList().addIdentifier("#online" );
				
			} else if (clickType == ClickType.RIGHT ) {
				this.getIdentifiersList().addIdentifier("#offline");
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
				.addLores(this.getIdentifiersList().getPreviewLore())
				.addLore(LangManager.getValue("help_delete_player_filter"))
				.build(), e -> {
					if(e.getClick() == ClickType.RIGHT) {
						this.getIdentifiersList().reset();
					}
				}));
		

	}

	public IdentifiersList getIdentifiersList() {
		return identifiersList;
	}
	public void setIdentifiersList(IdentifiersList identifiersList) {
		this.identifiersList = identifiersList;
	}
}
