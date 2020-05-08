package fr.bletrazer.mailbox.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.utils.LangManager;


public class SQLConnection {
	public static final String SGBD_TYPE_ROOT = "jdbc:mysql://";
	private static SQLConnection instance = new SQLConnection();

	private Connection connection;
	private String jdbc;
	private String host;
	private String database;
	private String user;
	private String password;

	public static SQLConnection getInstance() {
		return instance;
	}
	
	public void connect(String jdbc, String host, String database, String user, String password) {
		if (!isConnected()) {
			Main.getInstance().getLogger().log(Level.INFO, LangManager.getValue("string_sql_connection"));
			setJdbc(jdbc);
			setHost(host);
			setDatabase(database);
			setUser(user);
			setPassword(password);

			try {
				setConnection(DriverManager.getConnection(getJdbc() + getHost() + "/" + getDatabase() + "?useSSL=false", getUser(), getPassword()));
				Main.getInstance().getLogger().log(Level.INFO, LangManager.getValue("string_sql_connected"));
				
			} catch (SQLException e) {
				Main.getInstance().getLogger().log(Level.INFO, LangManager.getValue("string_sql_impossible_to_connect"));
			}
		}
	}
	
	public Boolean startTransaction() {
		Boolean res = false;
		try {
			if(this.isConnected() ) {
				this.getConnection().setAutoCommit(false);
				res = true;
			} else {
				Main.getInstance().getLogger().log(Level.SEVERE, LangManager.getValue("string_error_database_connection"));
			}
		} catch (SQLException e) {
			Main.getInstance().getLogger().log(Level.SEVERE, LangManager.getValue("string_error_sql"));
			e.printStackTrace();
		}
		
		return res;
	}
	
	public void rollBack() {
        try {
        	if(this.isConnected() ) {
	        	if(!this.getConnection().getAutoCommit() ) {
	                Main.getInstance().getLogger().log(Level.SEVERE, "Transaction is being rolled back");
	                this.getConnection().rollback();
	                
	        	}
        	} else {
        		Main.getInstance().getLogger().log(Level.SEVERE, LangManager.getValue("string_error_database_connection"));
        	}
        } catch(SQLException excep) {
            excep.printStackTrace();
        }
	}
	
	/*
	 * Commit transaction and close the parametrized query
	 */
	public Boolean commit(PreparedStatement query) {
		Boolean res = false;
		
		try {
			this.getConnection().commit();
			this.getConnection().setAutoCommit(true);
			query.close();
			
			res = true;
			
		} catch (SQLException e) {
			res = false;
	        e.printStackTrace();
	        

	    }
		
		return res;
	}
	
	public void disconnect() {
		if (isConnected()) {
			try {
				getConnection().close();
				Main.getInstance().getLogger().log(Level.INFO, LangManager.getValue("string_sql_disconnected"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void refresh() {
		try {
			if (isConnected()) {
				getConnection().close();
			}
			setConnection(DriverManager.getConnection(getJdbc() + getHost() + "/" + getDatabase() + "?useSSL=false", getUser(),
					getPassword()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		boolean isConnected = false;

		try {
			PreparedStatement query = getConnection().prepareStatement("SELECT 1");
			query.executeQuery();
			query.close();
			isConnected = true;
		} catch (Exception exception) {
		}

		return isConnected;
	}

	public Connection getConnection() {
		return this.connection;
	}

	private void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getJdbc() {
		return this.jdbc;
	}

	public void setJdbc(String jdbc) {
		this.jdbc = jdbc;
	}

	public String getHost() {
		return this.host;
	}

	private void setHost(String host) {
		this.host = host;
	}

	public String getDatabase() {
		return this.database;
	}

	private void setDatabase(String database) {
		this.database = database;
	}

	public String getUser() {
		return this.user;
	}

	private void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return this.password;
	}

	private void setPassword(String password) {
		this.password = password;
	}
}
