package fr.dornacraft.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.dornacraft.mailbox.DataManager.Data;
import fr.dornacraft.mailbox.DataManager.factories.DataFactory;


public class DataSQL extends DAO<Data> {
	
	private static final String TABLE_NAME = "MailBox_Data";
	
	public DataSQL() {
		super();

		try {
			PreparedStatement query = this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME
					+ " (id BIGINT NOT NULL AUTO_INCREMENT, uuid VARCHAR(255), author VARCHAR(255), object VARCHAR(255), creationDate TIMESTAMP, PRIMARY KEY(id))");
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
	 * table name format ?:
	 *  - [id: int] [uuid: String] [author: String] [object: String] [creationDate: Date]
	 * 
	 */
	
	/**
	 * Récupère la liste de data associé a l'uuid en paramètre
	 */
	public List<Data> getDataList(UUID uuid){
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
				res.add(tempData );
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
			DataFactory temp = new DataFactory(null, obj.getUuid(), obj.getAuthor(), obj.getObject(), Timestamp.from(Instant.now()));
			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, author, object, creationDate) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			query.setString(1, temp.getUuid().toString());
			query.setString(2, temp.getAuthor());
			query.setString(3, temp.getObject());
			query.setTimestamp(4, temp.getCreationDate());

			query.execute();

			ResultSet tableKeys = query.getGeneratedKeys();
			if(tableKeys.next()) {
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
	public Data find(Long i) {
		Data res = null;
		
		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();
			
			if(resultset.next() ) {
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
			query.setString(1, obj.getUuid().toString());
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
	public void delete(Data obj) {
		try {
			PreparedStatement query = super.getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1,  obj.getId());
			query.execute();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
