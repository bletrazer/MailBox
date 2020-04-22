package fr.bletrazer.mailbox.inventory;

import java.text.SimpleDateFormat;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.lang.LangManager;

public class MailBoxInventoryHandler {
	
	public static Material BORDER_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;
	public static Material DELETE_ALL_MATERIAL = Material.BARRIER;
	
	public static ItemStack generateItemRepresentation(Data data) {
		ItemStack res = null;
		
		if(data instanceof ItemData) {
			res = generateItemDataRepresentation((ItemData) data);
			
		} else if (data instanceof LetterData) {
			res = generateLetterDataRepresentation((LetterData) data);
		}
		
		
		return res;
	}
	
	private static ItemStack generateItemDataRepresentation(ItemData data) {
		return data.getItem();
	}
	
	private static	 ItemStack generateLetterDataRepresentation(LetterData data) {
		SimpleDateFormat sdf =  new SimpleDateFormat(LangManager.getValue("string_date_format"));
		ItemStackBuilder itemGenerator = new ItemStackBuilder(data.getLetterType().getMaterial())
				.setName(LangManager.getValue("string_object") + ": " + data.getObject())
				.addLore(LangManager.getValue("string_author") + ": " + data.getAuthor())
				.addLore(LangManager.getValue("string_reception_date") + ": " + sdf.format(data.getCreationDate()) );
		
		if(!data.getIsRead()) {
			itemGenerator.enchant(Enchantment.ARROW_FIRE, 1).addFlag(ItemFlag.HIDE_ENCHANTS);
		}
		
		return itemGenerator.build();
	}
	
}
