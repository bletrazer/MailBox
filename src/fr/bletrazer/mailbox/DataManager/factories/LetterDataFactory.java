package fr.bletrazer.mailbox.DataManager.factories;

import java.util.List;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;

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
		this.setOwnerUuid(data.getOwnerUuid() );
	}
	
}
