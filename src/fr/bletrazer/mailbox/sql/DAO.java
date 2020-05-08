package fr.bletrazer.mailbox.sql;

import java.sql.Connection;
import java.util.List;

public abstract class DAO<T> {

	private Connection connection = SQLConnection.getInstance().getConnection();
	
	public DAO() {
	}
	
	public abstract T create(T obj);
	
	public abstract List<T> createAll(List<T> list);
	
	public abstract T find(Long i);

	public abstract T update(T obj);

	public abstract Boolean delete(T obj);

	public Connection getConnection() {
		return connection;
	}
	

}
