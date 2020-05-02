package fr.bletrazer.mailbox.DataManager;

import java.sql.Timestamp;
import java.util.UUID;

import fr.bletrazer.mailbox.DataManager.factories.DataFactory;

public abstract class Data {

	private Long id;
	private String author;
	private String object;
	private Timestamp creationDate;
	private UUID ownerUuid;
	
	protected Data(Long id, UUID ownerUuid, String author, String object, Timestamp creationDate) {
		this.setId(id);
		this.setOwnerUuid(ownerUuid);
		this.setAuthor(author);
		this.setObject(object);
		this.setCreationDate(creationDate);
	}
	
	protected Data(UUID uuid, String author, String object) {
		this(null, uuid, author, object, null);
	}
	
	protected Data() {
		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	
	public Data clone() {
		return new DataFactory(this.getId(), this.getOwnerUuid(), this.getAuthor(), this.getObject(), this.getCreationDate());
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	public void setOwnerUuid(UUID ownerUuid) {
		this.ownerUuid = ownerUuid;
	}

}
