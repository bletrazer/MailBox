package fr.bletrazer.mailbox.inventory;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.bletrazer.mailbox.inventory.builders.ConfirmationInventoryBuilder;
import fr.minuskube.inv.content.InventoryContents;

public class ConfirmationInventory extends ConfirmationInventoryBuilder {
	
	private Consumer<BukkitTask> confirmation;
	private Consumer<BukkitTask> annulation;
	private Consumer<BukkitTask> update;
	
	public ConfirmationInventory(String subId, String title) {
		super(subId, title);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Consumer<InventoryClickEvent> onConfirmation(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Consumer<InventoryClickEvent> onAnnulation(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onUpdate(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub
		
	}

	public Consumer<BukkitTask> getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(Consumer<BukkitTask> confirmation) {
		this.confirmation = confirmation;
	}

	public Consumer<BukkitTask> getAnnulation() {
		return annulation;
	}

	public void setAnnulation(Consumer<BukkitTask> annulation) {
		this.annulation = annulation;
	}

	public Consumer<BukkitTask> getUpdate() {
		return update;
	}

	public void setUpdate(Consumer<BukkitTask> update) {
		this.update = update;
	}

}
