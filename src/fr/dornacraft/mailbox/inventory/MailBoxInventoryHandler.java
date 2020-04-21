package fr.dornacraft.mailbox.inventory;

import java.text.SimpleDateFormat;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.ItemData;
import fr.dornacraft.mailbox.DataManager.LetterData;

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
		SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
		ItemStackBuilder itemGenerator = new ItemStackBuilder(data.getLetterType().getMaterial()).setName("§r§7" + data.getObject()).setLoreFormat("§r§7")
				.addLore("Expéditeur: " + data.getAuthor()).addLore("date de reception: " + sdf.format(data.getCreationDate()) );
		
		if(!data.getIsRead()) {
			itemGenerator.enchant(Enchantment.ARROW_FIRE, 1).addFlag(ItemFlag.HIDE_ENCHANTS);
		}
		
		return itemGenerator.build();
	}
	
}
