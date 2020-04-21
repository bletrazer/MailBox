package fr.dornacraft.mailbox.sql;

import java.sql.Connection;

public abstract class DAO<T> {

	private Connection connection = SQLConnection.getInstance().getConnection();
	
	public DAO() {
	}
	
	public abstract T create(T obj);

	public abstract T find(Long i);

	public abstract T update(T obj);

	public abstract void delete(T obj);

	public Connection getConnection() {
		return connection;
	}
	

}
