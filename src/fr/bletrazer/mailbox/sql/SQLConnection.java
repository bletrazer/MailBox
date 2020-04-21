package fr.bletrazer.mailbox.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import fr.bletrazer.mailbox.Main;


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
			Main.getInstance().getLogger().log(Level.INFO, "Tentative de connexion à la base de donnée.");
			setJdbc(jdbc);
			setHost(host);
			setDatabase(database);
			setUser(user);
			setPassword(password);

			try {
				setConnection(DriverManager.getConnection(getJdbc() + getHost() + "/" + getDatabase() + "?useSSL=false",
						getUser(), getPassword()));
				Main.getInstance().getLogger().log(Level.INFO, "Base de donnée connectée.");
			} catch (SQLException e) {
				Main.getInstance().getLogger().log(Level.INFO, "Connexion à la base de donnée impossible.");
			}
		}
	}

	public void disconnect() {
		if (isConnected()) {
			try {
				getConnection().close();
				Main.getInstance().getLogger().log(Level.INFO, "Base de donnée connectée.");
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
