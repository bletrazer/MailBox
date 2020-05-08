package fr.bletrazer.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;
import fr.bletrazer.mailbox.utils.LangManager;

public class DataSQL extends DAO<Data> {

	private static final String TABLE_NAME = "MailBox_Data";

	public DataSQL() {
		super();

		try {
			PreparedStatement query = this.getConnection().prepareStatement(
					"CREATE TABLE IF NOT EXISTS	" + TABLE_NAME + " (id BIGINT NOT NULL AUTO_INCREMENT, uuid VARCHAR(255), author VARCHAR(255), object VARCHAR(255), creationDate TIMESTAMP, PRIMARY KEY(id))");
			query.executeUpdate();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static DataSQL INSTANCE = new DataSQL();

	public static DataSQL getInstance() {
		return INSTANCE;
	}

	/*
	 * table name format ?: - [id: int] [uuid: String] [author: String] [object:
	 * String] [creationDate: Date]
	 * 
	 */

	/**
	 * Récupère la liste de data associé a l'uuid en paramètre
	 */
	public List<Data> getDataList(UUID uuid) {
		List<Data> res = new ArrayList<>();

		try {
			PreparedStatement query = getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?");
			query.setString(1, uuid.toString());
			ResultSet rs = query.executeQuery();

			while (rs.next()) {
				Long tempId = rs.getLong("id");
				UUID tempUuid = UUID.fromString(rs.getString("uuid"));
				String tempAuthor = rs.getString("author");
				String tempObject = rs.getString("object");
				Timestamp tempCreationDate = rs.getTimestamp("creationDate");

				Data tempData = new DataFactory(tempId, tempUuid, tempAuthor, tempObject, tempCreationDate);
				res.add(tempData);
			}

			query.close();

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return res;
	}

	@Override
	public Data create(Data obj) {
		Data res = null;

		try {
			DataFactory temp = new DataFactory(null, obj.getOwnerUuid(), obj.getAuthor(), obj.getObject(), Timestamp.from(Instant.now()));
			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, author, object, creationDate) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			query.setString(1, temp.getOwnerUuid().toString());
			query.setString(2, temp.getAuthor());
			query.setString(3, temp.getObject());
			query.setTimestamp(4, temp.getCreationDate());

			query.execute();

			ResultSet tableKeys = query.getGeneratedKeys();
			if (tableKeys.next()) {
				temp.setId(tableKeys.getLong(1));
			}

			query.close();
			res = temp;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public List<Data> createAll(List<Data> list) {
		List<Data> res = null;
		PreparedStatement query = null;

		try {
			query = this.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, author, object, creationDate) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			List<Data> temp = new ArrayList<>();

			for (Integer index = 0; index < list.size(); index++) {
				Data tempData = list.get(index).clone();
				tempData.setCreationDate(Timestamp.from(Instant.now()));
				query.setString(1, tempData.getOwnerUuid().toString());
				query.setString(2, tempData.getAuthor());
				query.setString(3, tempData.getObject());
				query.setTimestamp(4, tempData.getCreationDate());

				query.execute();

				ResultSet tableKeys = query.getGeneratedKeys();
				if (tableKeys.next()) {
					tempData.setId(tableKeys.getLong(1));
				}

				temp.add(tempData);

			}

			res = temp;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Data find(Long i) {
		Data res = null;

		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();

			if (resultset.next()) {
				UUID uuid = UUID.fromString(resultset.getString("uuid"));
				String author = resultset.getString("author");
				String object = resultset.getString("object");
				Timestamp creationDate = resultset.getTimestamp("creationDate");
				res = new DataFactory(i, uuid, author, object, creationDate);

			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Data update(Data obj) {
		Data res = null;
		try {
			PreparedStatement query = super.getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET uuid = ?, author = ?, object = ?, creationDate = ? WHERE id = ?");
			query.setString(1, obj.getOwnerUuid().toString());
			query.setString(2, obj.getAuthor());
			query.setString(3, obj.getObject());
			query.setTimestamp(4, obj.getCreationDate());
			query.setLong(5, obj.getId());

			query.executeUpdate();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Boolean delete(Data obj) {
		Boolean res = false;

		try {
			if (!SQLConnection.getInstance().isConnected()) {
				Main.getInstance().getLogger().log(Level.SEVERE, LangManager.getValue("string_error_database_connection"));
			}
			
			PreparedStatement query = super.getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, obj.getId());
			query.execute();
			query.close();
			res = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

}
