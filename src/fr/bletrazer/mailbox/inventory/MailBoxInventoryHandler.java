package fr.bletrazer.mailbox.inventory;

import java.text.SimpleDateFormat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;

public class MailBoxInventoryHandler {
	
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
				.setLoreFormat("§f")
				.addAutoFormatingLore("§e§l" + LangManager.getValue("string_object") +":§r§f " + data.getObject(), 35)
				.addAutoFormatingLore("§e§l" + LangManager.getValue("string_author") +":§r§f " + data.getAuthor(), 35)
				.addAutoFormatingLore("§e§l" + LangManager.getValue("string_reception_date") +":§r§f " + sdf.format(data.getCreationDate()), 35 )
				.addLore(" ");
		
		if(!data.getIsRead()) {
			itemGenerator.enchant(Enchantment.ARROW_FIRE, 1).addFlag(ItemFlag.HIDE_ENCHANTS);
		}
		
		return itemGenerator.build();
	}
	
}
