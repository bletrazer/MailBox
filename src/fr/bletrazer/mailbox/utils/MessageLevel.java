package fr.bletrazer.mailbox.utils;

public enum MessageLevel {
	
	INFO("§e"),
	NOTIFICATION("§a"),
	ERROR("§c");
	
	private String color;
	
	private MessageLevel(String str) {
		this.setColor(str);
	}

	public String getColor() {
		return color;
	}

	private void setColor(String color) {
		this.color = color;
	}
	
}
