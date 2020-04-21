package fr.dornacraft.mailbox.DataManager;

import org.bukkit.Material;

public enum LetterType {
	
	NO_TYPE(Material.GLASS),
	STANDARD(Material.PAPER),
	SYSTEM(Material.MAP),
	ANNOUNCE(Material.FILLED_MAP);
	
	private Material material;
	
	private LetterType(Material mat) {
		this.setMaterial(mat);
	}

	public Material getMaterial() {
		return material;
	}

	private void setMaterial(Material material) {
		this.material = material;
	}
}
