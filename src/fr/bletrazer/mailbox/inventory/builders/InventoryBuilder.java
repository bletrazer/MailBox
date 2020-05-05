package fr.bletrazer.mailbox.inventory.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.inventory.inventories.utils.OptionalClickableItem;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.SmartInventory.Builder;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;

public abstract class InventoryBuilder implements InventoryProvider { 
	public static Material GO_BACK_MATERIAL = Material.OAK_SIGN;
	public Material PAGINATION_MATERIAL = Material.ARROW;
	
	private String id;
	private String title;
	private Integer rows;
	
	private InventoryBuilder parent;
	private Consumer<BukkitTask> onFinalQuit;
	private Boolean finalClose = true;

	private List<OptionalClickableItem> fixeOptions = new ArrayList<>();
	private List<OptionalClickableItem> staticOptions = new ArrayList<>();
	
	public InventoryBuilder(String id, String title, Integer rows) {
		this.setId(id);
		this.setTitle(title);
		this.setRows(rows);
		
	}
	
	protected Builder getBuilder() {
		Builder res = SmartInventory.builder().manager(Main.getManager());
		
		res.id(this.getId() )
		        .provider(this)
		        .size(this.getRows(), 9)
		        .title(this.getTitle())
		        .closeable(this.isFinalClose());
		
		if (this.getFinalQuitTask() != null) {
			res.listener(new InventoryListener<InventoryCloseEvent>(InventoryCloseEvent.class, e -> {
				if (this.isFinalClose()) {
					Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), this.getFinalQuitTask() );
				}
			}));
		}
		
		return res;
	}
	
	public void openInventory(Player player) {
		Bukkit.getScheduler().runTask(Main.getInstance(), e -> {
			this.setFinalClose(true);
			SmartInventory inv = this.getBuilder().build();
			inv.open(player);
		});
	}
	
	public void returnToParent(Player player) {
		if(this.getParent() != null) {
			this.getParent().openInventory(player);
			
		} else {
			player.closeInventory();
		}
		
	}
	
	public ClickableItem nextPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(PAGINATION_MATERIAL).setName("§e§l" + LangManager.getValue("string_next_page") ).build(), e -> inventory.open(player, pagination.next().getPage()));
	}
	
	public ClickableItem previousPageItem(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();
		SmartInventory inventory = contents.inventory();
		return ClickableItem.of(new ItemStackBuilder(PAGINATION_MATERIAL).setName("§e§l" + LangManager.getValue("string_previous_page")).build(), e -> inventory.open(player, pagination.previous().getPage()));
	}
	
	public ClickableItem goBackItem(Player player) {
		
		String name = this.getParent() == null ? "§c§l"+LangManager.getValue("string_quit") : "<- §c§l" + LangManager.getValue("string_previous_menu");

		return ClickableItem.of(new ItemStackBuilder(GO_BACK_MATERIAL).setName(name).build(), goBackListener(player));
	}
	
	public Consumer<InventoryClickEvent> goBackListener(Player player){
		return e -> {
			if (this.getParent() != null) {
				this.getParent().openInventory(player);

			} else {
				player.closeInventory();
			}

		};
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
		this.initializeInventory(p, c);
		for(OptionalClickableItem oci : this.getFixeOptions() ) {
			c.set(oci.getRow(), oci.getColumn(), oci.getClickable());
		}
	}

	@Override
	public void update(Player p, InventoryContents c) {
		this.updateInventory(p, c);
		
		for(OptionalClickableItem oci : this.getStaticOptions() ) {
			c.set(oci.getRow(), oci.getColumn(), oci.getClickable());
		}
		
	}

	public InventoryBuilder getParent() {
		return parent;
	}

	public InventoryBuilder setParent(InventoryBuilder parent) {
		this.parent = parent;
		return this;
	}

	public Boolean isFinalClose() {
		return finalClose;
	}

	public InventoryBuilder setFinalClose(Boolean finalClose) {
		this.finalClose = finalClose;
		return this;
	}

	public Consumer<BukkitTask> getFinalQuitTask() {
		return onFinalQuit;
	}

	public void onFinalClose(Consumer<BukkitTask> onFinalQuit) {
		this.onFinalQuit = onFinalQuit;
	}

	private List<OptionalClickableItem> getFixeOptions() {
		return fixeOptions;
	}

	private List<OptionalClickableItem> getStaticOptions() {
		return staticOptions;
	}
	
	public void addOption(OptionalClickableItem oci) {
		if(oci.doUpdate() ) {
			this.getStaticOptions().add(oci);
			
		} else {
			this.getFixeOptions().add(oci);
		}
			
	}
	
}