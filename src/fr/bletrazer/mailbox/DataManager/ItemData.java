package fr.bletrazer.mailbox.DataManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class ItemData extends Data {

	private Duration duration;
	private ItemStack item;

	public ItemData(UUID uuid, String author, String object, ItemStack itemstack, Duration duration) {
		super(uuid, author, object);
		this.setItem(itemstack);
		this.setDuration(duration);
	}
	
	public ItemData(Data data, ItemStack itemstack, Duration duration) {
		super(data.getId(), data.getOwnerUuid(), data.getAuthor(), data.getObject(), data.getCreationDate());
		this.setItem(itemstack);
		this.setDuration(duration);
		
	}
	
	public Boolean isOutOfDate() {
		Boolean res = false;
		
		if(!this.getDuration().isZero() ) {
			LocalDateTime date = this.getCreationDate().toLocalDateTime();
			LocalDateTime added = date.plus(this.getDuration());
			res = added.compareTo(LocalDateTime.now()) < 0 ;
		}
		
		
		return res;
	}

	public Duration getDuration() {
		return duration;
	}

	private void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	public ItemStack getItem() {
		return item;
	}

	private void setItem(ItemStack item) {
		this.item = item;
	}

	public ItemData clone() {
		Data data = super.clone();
		ItemData res = new ItemData(data, this.getItem(), this.getDuration());
		return res;
	}

}
