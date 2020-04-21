package fr.dornacraft.mailbox.playerManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.dornacraft.mailbox.sql.SQLConnection;

public class PlayerInfoSQL {
	
	private static final String TABLE_NAME = "MailBox_PlayerInfo";
	
	public PlayerInfoSQL() {
		
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME
					+ " (uuid VARCHAR(255), name VARCHAR(255))");
			query.executeUpdate();
			query.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static PlayerInfoSQL INSTANCE = new PlayerInfoSQL();
	public static PlayerInfoSQL getInstance() {
		return INSTANCE;
	}
	
	public PlayerInfo tryRegister(Player player) {
		PlayerInfo PI = new PlayerInfo(player);
		Integer check = PlayerInfoSQL.getInstance().check(PI);
		
		if(check == -1 ) {
			PlayerInfoSQL.getInstance().create(PI);
			
		} else if (check == 0) {
			PlayerInfoSQL.getInstance().update(PI);
			
		}
		
		return PI;
	}
	
	public List<PlayerInfo> getAll(){
		List<PlayerInfo> res = new ArrayList<>();
		
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME);
			ResultSet rs = query.executeQuery();
			
			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String name = rs.getString("name");
				
				PlayerInfo playerInfo = new PlayerInfo(name, uuid);
				
				res.add(playerInfo );
			}
			
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();

		}
		
		
		return res;
	}
	
	public PlayerInfo create(PlayerInfo obj) {
		PlayerInfo res = null;
		
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, name) VALUES(?, ?)");
			query.setString(1, obj.getUuid().toString());
			query.setString(2, obj.getName());

			query.execute();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		
		return res;
	}

	/**
	 * 
	 * @param obj
	 * @return -1: n'existe pas<br> 0: existe mais pas a jour<br> 1: existe tout est bon
	 */
	public Integer check(PlayerInfo obj) {
		Integer res = -1;
		
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ? ");
			query.setString(1, obj.getUuid().toString() );
			ResultSet resultset = query.executeQuery();
			
			if(resultset.next() ) {
				if(resultset.getString("name").equals(obj.getName()) ) {
					res = 1;
					
				} else {
					res = 0;
				}
				
			}
			
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		
		return res;
	}

	public PlayerInfo update(PlayerInfo obj) {
		PlayerInfo res = null;
		
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET name = ? WHERE id = ?");
			query.setString(1, obj.getName());
			query.setString(2, obj.getUuid().toString() );

			query.executeUpdate();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public void delete(PlayerInfo obj) {
		try {
			PreparedStatement query = SQLConnection.getInstance().getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE uuid = ?");
			query.setString(1,  obj.getUuid().toString() );
			query.execute();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
