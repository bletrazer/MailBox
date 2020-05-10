package fr.bletrazer.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.factories.LetterDataFactory;

public class LetterDataSQL extends DAO<LetterData> {
	private static final String TABLE_NAME = "MailBox_LetterData";
	private static LetterDataSQL INSTANCE = new LetterDataSQL();

	public static LetterDataSQL getInstance() {
		return INSTANCE;
	}

	private LetterDataSQL() {
		super();

		try {
			PreparedStatement query = this.getConnection().prepareStatement(
					"CREATE TABLE IF NOT EXISTS	" + TABLE_NAME + " (id BIGINT NOT NULL, type VARCHAR(255) NOT NULL, content TEXT NOT NULL, isRead BOOLEAN NOT NULL DEFAULT '0', PRIMARY KEY(id))");
			query.executeUpdate();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transforme une liste de string en string unique et les séparé par " #-# "
	 */
	public String toText(List<String> list) {
		StringBuilder sb = new StringBuilder();
		String res = "";

		if (list != null) {
			for (String page : list) {
				sb.append(String.format("%s#-#", page == null || page.isEmpty() ? " " : page));
			}
			res = sb.toString();

		}

		return res;
	}

	/**
	 * Transforme un String en List en utilisant "\n" comme s�parateur
	 */
	private List<String> fromText(String str) {
		return Arrays.asList(StringUtils.split(str, "#-#"));
	}

	/*
	 * Format de la table: [id: int] [type: String(LetterType name)] [content:
	 * String] [isRead: Boolean]
	 * 
	 */

	@Override
	public LetterData create(LetterData obj) {
		LetterData res = null;

		try {
			LetterData temp = obj.clone();
			Data data = DataSQL.getInstance().create(temp);

			if (data != null) {
				temp.setId(data.getId());
				temp.setCreationDate(data.getCreationDate());

				PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?)");
				query.setLong(1, temp.getId());
				query.setString(2, temp.getLetterType().name());
				query.setString(3, toText(temp.getContent()));
				query.setBoolean(4, temp.getIsRead());

				query.execute();
				query.close();

				res = temp;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public List<LetterData> createAll(List<LetterData> list) {
		List<LetterData> res = null;
		Boolean transaction = SQLConnection.getInstance().startTransaction();

		if (transaction) {
			List<LetterData> temp = super.createAll(list);

			if (temp != null ) {
				if(SQLConnection.getInstance().commit()) {
					res = temp;
				}
			}
		}

		return res;
	}

	/*
	 * table name format ?: - [id: int] [type: String(LetterType name)] [content:
	 * String] [isRead: Boolean]
	 * 
	 */

	@Override
	public LetterData find(Long i) {
		LetterData res = null;

		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();

			if (resultset.next()) {
				Data data = DataSQL.getInstance().find(i);
				LetterType type = LetterType.valueOf(resultset.getString("type"));
				List<String> content = fromText(resultset.getString("content"));
				Boolean isRead = resultset.getBoolean("isRead");

				if (data != null) {
					res = new LetterDataFactory(data, type, content, isRead);

				}

			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Boolean update(LetterData obj) {
		Boolean res = false;

		if (SQLConnection.getInstance().startTransaction()) {
			if (DataSQL.getInstance().update(obj) ) {
				try {
					PreparedStatement query = super.getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET content = ?, type = ?, isRead = ? WHERE id = ?");
					query.setString(1, toText(obj.getContent()));
					query.setString(2, obj.getLetterType().name());
					query.setBoolean(3, obj.getIsRead());
					query.setLong(4, obj.getId());

					query.executeUpdate();
					query.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			if (SQLConnection.getInstance().commit()) {
				res = true;
			}

		}

		return res;
	}

	@Override
	public Boolean delete(Long id) {
		Boolean res = false;

		if (SQLConnection.getInstance().startTransaction()) {
			if (DataSQL.getInstance().delete(id)) {
				try {
					PreparedStatement query = super.getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
					query.setLong(1, id);
					query.execute();
					query.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				if(SQLConnection.getInstance().commit() ) {
					res = true;
				}
			}
		}

		return res;
	}
	
	@Override
	public Boolean deleteAll(List<Long> list) {
		Boolean res = false;

		if (SQLConnection.getInstance().startTransaction() ) {
			if (super.deleteAll(list) && SQLConnection.getInstance().commit()) {
				res = true;
			}
		}

		return res;
	}

}
