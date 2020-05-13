package fr.bletrazer.mailbox.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.bletrazer.mailbox.DataManager.Data;

public abstract class BaseSQL<T extends Data> {
	private static final SQLConnection sqlConnection = SQLConnection.getInstance();

	protected SQLConnection getSqlConnection() {
		return sqlConnection;
	}

	protected abstract T onCreate(T obj);

	public T create(T obj) {
		T res = null;

		if (this.getSqlConnection().startTransaction()) {
			T temp = this.onCreate(obj);

			if (temp != null) {
				if (this.getSqlConnection().commit()) {
					res = temp;
				}
			}
		}

		return res;
	}

	public List<T> createAll(List<T> list) {
		List<T> res = null;

		if (this.getSqlConnection().startTransaction()) {
			List<T> temp = new ArrayList<>();

			for (T obj : list) {
				T TTemp = this.onCreate(obj);

				if (TTemp == null) {
					temp = null;
					break;

				} else {
					temp.add(TTemp);
				}
			}

			if (temp != null) {
				if (this.getSqlConnection().commit()) {
					res = temp;
				}
			}
		}

		return res;
	}

	protected abstract List<T> onFind(UUID uuid);

	public List<T> find(UUID uuid) {
		List<T> res = null;

		if (this.getSqlConnection().isConnected()) {
			List<T> temp = this.onFind(uuid);

			if (temp != null) {
				res = temp;
			}

		}

		return res;
	}

	protected abstract T onUpdate(Long id, T obj);

	public T update(Long id, T obj) {
		T res = null;

		if (this.getSqlConnection().startTransaction()) {
			T temp = this.onUpdate(id, obj);

			if (temp != null) {
				if (this.getSqlConnection().commit()) {
					res = temp;
				}
			}

		}

		return res;
	}

	public List<T> updateAll(List<T> list) {
		List<T> res = null;

		if (this.getSqlConnection().startTransaction()) {
			List<T> temp = new ArrayList<>();

			for (T obj : list) {
				T tTemp = this.onUpdate(obj.getId(), obj);

				if (tTemp != null) {
					temp.add(tTemp);

				} else {
					temp = null;
					break;
				}
			}

			if (temp != null) {
				if (this.getSqlConnection().commit()) {
					res = temp;
				}
			}
		}

		return res;
	}

	protected abstract Boolean onDelete(T obj);

	public Boolean delete(T obj) {
		Boolean res = false;

		if (this.getSqlConnection().startTransaction()) {

			if (this.onDelete(obj)) {

				if (this.getSqlConnection().commit()) {
					res = true;

				}
			}
		}
		return res;
	}

	public Boolean deleteAll(List<T> list) {
		Boolean res = false;

		if (this.getSqlConnection().startTransaction()) {
			Boolean temp = true;

			for (T obj : list) {
				Boolean TTemp = this.onDelete(obj);

				if (!TTemp) {
					temp = false;
					break;

				}
			}

			if (temp) {
				if (this.getSqlConnection().commit()) {
					res = temp;
				}
			}
		}

		return res;
	}

}