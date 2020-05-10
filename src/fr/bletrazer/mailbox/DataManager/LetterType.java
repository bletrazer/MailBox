package fr.bletrazer.mailbox.DataManager;

import org.bukkit.Material;

import fr.bletrazer.mailbox.utils.LangManager;

public enum LetterType {
	
	NO_TYPE(Material.WRITABLE_BOOK, LangManager.getValue("string_empty")),
	STANDARD(Material.PAPER, LangManager.getValue("string_standard_type")),
	SYSTEM(Material.MAP, LangManager.getValue("string_system_type")),
	ANNOUNCE(Material.BOOK, LangManager.getValue("string_announce_type"));
	
	private Material material;
	private String translation;
	
	private LetterType(Material mat, String translation) {
		this.setMaterial(mat);
		this.setTranslation(translation);
	}

	public Material getMaterial() {
		return material;
	}

	private void setMaterial(Material material) {
		this.material = material;
	}

	public String getTranslation() {
		return translation;
	}

	private void setTranslation(String translation) {
		this.translation = translation;
	}
}