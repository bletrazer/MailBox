package fr.bletrazer.mailbox.inventory.inventories.utils;

import fr.minuskube.inv.ClickableItem;

public class OptionalClickableItem {
	
	private Integer column;
	private Integer row;
	private Boolean update = false;
	private ClickableItem clickable;
	
	public OptionalClickableItem(Integer row, Integer colum, ClickableItem clickable) {
		this.setRow(row);
		this.setColumn(colum);
		this.setClickable(clickable);
	}

	public Integer getColumn() {
		return column;
	}

	private void setColumn(Integer column) {
		this.column = column;
	}

	public Integer getRow() {
		return row;
	}

	private void setRow(Integer row) {
		this.row = row;
	}

	public ClickableItem getClickable() {
		return clickable;
	}

	private void setClickable(ClickableItem clickable) {
		this.clickable = clickable;
	}

	public boolean doUpdate() {
		return update;
	}

	public void setUpdate(Boolean update) {
		this.update = update;
	}
	
	
}
