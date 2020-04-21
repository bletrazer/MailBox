package fr.dornacraft.mailbox.DataManager.factories;

import java.util.List;

import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.LetterData;
import fr.dornacraft.mailbox.DataManager.LetterType;

public class LetterDataFactory extends LetterData {
	
	public LetterDataFactory() {
		super();
	}
	
	public LetterDataFactory(Data data, LetterType type, List<String> content, Boolean isRead) {
		super(data, type, content, isRead);
		
	}
	
	public void setData(Data data) {
		this.setId(data.getId());
		this.setAuthor(data.getAuthor() );
		this.setObject(data.getObject() );
		this.setCreationDate(data.getCreationDate());
		this.setUuid(data.getUuid() );
	}
	
}
