package fr.bletrazer.mailbox.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import fr.bletrazer.mailbox.DataManager.Data;

public abstract class DAO<T extends Data> {

	private Connection connection = SQLConnection.getInstance().getConnection();
	
	public DAO() {
	}
	
	public abstract T create(T obj);
	
	public List<T> createAll(List<T> list) {
		List<T> res = new ArrayList<>();

		for (Integer index = 0; index < list.size(); index++) {
			T temp = this.create(list.get(index) );
			
			if(temp != null) {
				res.add(temp);
				
			} else {
				res = null;
				break;
			}

		}

		return res;
	}
	
	public abstract T find(Long i);

	public abstract Boolean update(T obj);

	public abstract Boolean delete(Long id);
	
	public Boolean deleteAll(List<Long> idList) {
		Boolean res = true;

		for (Integer index = 0; index < idList.size(); index++) {
			if(!this.delete(idList.get(index) ) ) {
				res = false;
				break;
			}

		}

		return res;
	}

	public Connection getConnection() {
		return connection;
	}
	

}
