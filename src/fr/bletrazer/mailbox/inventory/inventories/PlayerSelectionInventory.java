package fr.bletrazer.mailbox.inventory.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.bletrazer.mailbox.inventory.builders.InventoryBuilder;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiersList;
import fr.bletrazer.mailbox.listeners.utils.ChatHooker;
import fr.bletrazer.mailbox.listeners.utils.hookers.CH_Player;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;

public class PlayerSelectionInventory extends InventoryBuilder {
	public static final String ID = "MailBox_Player_Selector";
	public static final Material CHOOSE_ALL_MATERIAL = Material.NETHER_STAR;
	public static final Material CHOOSE_FACTION_MATERIAL = Material.MAGENTA_BANNER;
	public static final Material CHOOSE_PRECISE_PLAYER_MATERIAL = Material.PLAYER_HEAD;
	private static final String PRECISE = LangManager.getValue("string_choose_precise_player");
	private static final String CHOOSE_SERVER = LangManager.getValue("string_choose_server");
	private static final String CHOOSE_ONLINE = LangManager.getValue("string_choose_server_online");
	private static final String CHOOSE_OFFLINE = LangManager.getValue("string_choose_server_offline");
	private static final String DISPLAYED_PLAYERS = LangManager.getValue("string_displayed_players");
	private static final String HELP_DELETE_FILTER = LangManager.getValue("help_delete_player_filter");
	
	private IdentifiersList identifiersList;
	private Boolean filterMode = true;
	
	public PlayerSelectionInventory(IdentifiersList identifiersList, String invTitle, InventoryBuilder parent) {
		super(ID, invTitle, 3);
		super.setParent(parent);
		this.setIdentifiersList(identifiersList);
	}
	
	@Override
	public void initializeInventory(Player player, InventoryContents contents) {
		if (this.getFilterMode() || player.hasPermission("mailbox.send.annouce")) {
			
			contents.set(1, 6, ClickableItem.of(new ItemStackBuilder(CHOOSE_ALL_MATERIAL)
					.setName("§f§l" + CHOOSE_SERVER)
					.addAutoFormatingLore(CHOOSE_ONLINE, 35)
					.addAutoFormatingLore(CHOOSE_OFFLINE, 35)
					.build(), e -> {
				ClickType clickType = e.getClick();

				if (clickType == ClickType.LEFT) {
					this.getIdentifiersList().addIdentifier("#online");

				} else if (clickType == ClickType.RIGHT) {
					this.getIdentifiersList().addIdentifier("#offline");
				}

			}));
		}
		
		contents.set(1, 2, ClickableItem.of(new ItemStackBuilder(CHOOSE_PRECISE_PLAYER_MATERIAL).setName("§f§l"+PRECISE).build(), e -> {
			if(e.getClick() == ClickType.LEFT ) {
				ChatHooker chatHooker = ChatHooker.get(player.getUniqueId());
				
				if(chatHooker == null) {
					chatHooker = new CH_Player(this.getIdentifiersList(), this);
					player.closeInventory();
					chatHooker.start(player);
					
				}
			}
		}));
	}

	@Override
	public void updateInventory(Player player, InventoryContents contents) {
        int state = contents.property("state", 0);
        contents.setProperty("state", state + 1);

        if(state % 20 != 0) {
        	return;
        }
        
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(Material.REDSTONE)
				.setName("§f§l"+DISPLAYED_PLAYERS+":")
				.addLores(this.getIdentifiersList().getPreviewLore())
				.addLore(HELP_DELETE_FILTER)
				.build(), e -> {
					if(e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP ) {
						this.getIdentifiersList().clear();
					}
				}));
		

	}

	public IdentifiersList getIdentifiersList() {
		return identifiersList;
	}
	public void setIdentifiersList(IdentifiersList identifiersList) {
		this.identifiersList = identifiersList;
	}

	public Boolean getFilterMode() {
		return filterMode;
	}

	public void setFilterMode(Boolean filterMode) {
		this.filterMode = filterMode;
	}
}
