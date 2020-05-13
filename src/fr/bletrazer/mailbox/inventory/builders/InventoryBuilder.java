package fr.bletrazer.mailbox.inventory.builders;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.SmartInventory.Builder;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;

public abstract class InventoryBuilder implements InventoryProvider {
	private static final String NEXT_PAGE = LangManager.getValue("string_next_page");
	private static final String PREVIOUS_PAGE = LangManager.getValue("string_previous_page");
	private static final String PREVIOUS_MENU = LangManager.getValue("string_previous_menu");
	private static final String QUIT = LangManager.getValue("string_quit");

	private String id;
	private String title;
	private Integer rows;

	private InventoryBuilder parent;
	private Boolean showReturnButton = true;

	public InventoryBuilder(String id, String title, Integer rows) {
		this.setId(id);
		this.setTitle(title);
		this.setRows(rows);

	}

	protected Builder getBuilder() {
		Builder res = SmartInventory.builder().manager(Main.getManager());

		res.id(this.getId()).provider(this).size(this.getRows(), 9).title(this.getTitle());

		return res;
	}

	public void openInventory(Player player) {
		Bukkit.getScheduler().runTask(Main.getInstance(), e -> {
			SmartInventory inv = this.getBuilder().build();
			inv.open(player);
		});
	}

	public void returnToParent(Player player) {
		if (this.getParent() != null) {
			this.getParent().openInventory(player);

		} else {
			player.closeInventory();
		}

	}

	public ClickableItem nextPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(Material.ARROW).setName("§e§l" + NEXT_PAGE).build(), e -> inventory.open(player, pagination.next().getPage()));
	}

	public ClickableItem previousPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(Material.ARROW).setName("§e§l" + PREVIOUS_PAGE).build(), e -> inventory.open(player, pagination.previous().getPage()));
	}

	public ClickableItem goBackItem(Player player) {
		String name = this.getParent() == null ? "§c§l" + QUIT : "<- §c§l" + PREVIOUS_MENU;
		return ClickableItem.of(new ItemStackBuilder(Material.OAK_SIGN).setName(name).build(), e -> {
			returnToParent(player);

		});
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	protected void setTitle(String title) {
		this.title = title;
	}

	public Integer getRows() {
		return rows;
	}

	protected void setRows(Integer rows) {
		this.rows = rows;
	}

	public abstract void initializeInventory(Player player, InventoryContents contents);

	public abstract void updateInventory(Player player, InventoryContents contents);

	@Override
	public void init(Player p, InventoryContents c) {
		if (this.getShowReturnButton()) {
			c.set(this.getRows() - 1, 0, this.goBackItem(p));
		}

		this.initializeInventory(p, c);
	}

	@Override
	public void update(Player p, InventoryContents c) {
		this.updateInventory(p, c);

	}

	public InventoryBuilder getParent() {
		return parent;
	}

	public InventoryBuilder setParent(InventoryBuilder parent) {
		this.parent = parent;
		return this;
	}

	public Boolean getShowReturnButton() {
		return this.showReturnButton;
	}

	public void setShowReturnButton(Boolean showReturnButton) {
		this.showReturnButton = showReturnButton;
	}

}