package fr.bletrazer.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;

public class LetterDataSQL extends BaseSQL<LetterData> {
	private final String TABLE_NAME = "MailBox_LetterData";
	private static final LetterDataSQL INSTANCE = new LetterDataSQL();

	public static LetterDataSQL getInstance() {
		return INSTANCE;
	}

	private LetterDataSQL() {
		super();
		
		if(this.getSqlConnection().isConnected() ) {
			try {
				PreparedStatement query = this.getSqlConnection().getConnection()
						.prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME + " (id BIGINT, uuid VARCHAR(255), type VARCHAR(255), content TEXT, isRead BOOLEAN DEFAULT '0', PRIMARY KEY(id))");
				query.executeUpdate();
				query.close();
	
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
	 * Transforme un String en List en utilisant "#-#" comme séparateur
	 */
	private List<String> fromText(String str) {
		return Arrays.asList(StringUtils.split(str, "#-#"));
	}

	@Override
	protected LetterData onCreate(LetterData obj) {
		LetterData res = null;
		LetterData temp = obj.clone();

		Data data = DataSQL.getInstance().create(temp);

		if (data != null) {
			temp.setId(data.getId());
			temp.setCreationDate(data.getCreationDate());

			try {
				PreparedStatement query = this.getSqlConnection().getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?)");
				query.setLong(1, temp.getId());
				query.setString(2, obj.getOwnerUuid().toString());
				query.setString(3, temp.getLetterType().name());
				query.setString(4, toText(temp.getContent()));
				query.setBoolean(5, temp.getIsRead());

				query.execute();
				query.close();
				
				res = temp;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return res;
	}

	@Override
	protected List<LetterData> onFind(UUID uuid) {
		List<LetterData> res = null;
		List<LetterData> temp = new ArrayList<>();
		List<Data> dataList = DataSQL.getInstance().onFind(uuid);

		if (dataList != null) {
			try {
				PreparedStatement query = this.getSqlConnection().getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?");
				query.setString(1, uuid.toString());
				ResultSet resultset = query.executeQuery();

				while (resultset.next()) {
					LetterType type = LetterType.valueOf(resultset.getString("type"));
					List<String> content = fromText(resultset.getString("content"));
					Boolean isRead = resultset.getBoolean("isRead");
					Long id = resultset.getLong("id");

					Data tData = dataList.stream().filter(e -> e.getId() == id).findAny().orElse(new DataFactory());

					temp.add(new LetterData(tData, type, content, isRead));

				}

				query.close();
				
				res = temp;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return res;
	}
	
	@Override
	protected LetterData onUpdate(Long id, LetterData obj) {
		LetterData res = null;
		LetterData temp = obj.clone();
		Data uData = DataSQL.getInstance().onUpdate(id, obj);

		if (uData != null) {
			try {
				PreparedStatement query = this.getSqlConnection().getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET uuid = ?, content = ?, type = ?, isRead = ? WHERE id = ?");
				query.setString(1, obj.getOwnerUuid().toString());
				query.setString(2, toText(obj.getContent()));
				query.setString(3, obj.getLetterType().name());
				query.setBoolean(4, obj.getIsRead());
				query.setLong(5, id);

				query.executeUpdate();
				query.close();
				temp.setId(id);
				
				res = temp;

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		return res;
	}

	@Override
	protected Boolean onDelete(LetterData obj) {
		Boolean res = false;

		if (DataSQL.getInstance().delete(obj)) {
			try {
				PreparedStatement query = this.getSqlConnection().getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
				query.setLong(1, obj.getId());
				query.execute();
				query.close();
				res = true;

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return res;
	}

}
