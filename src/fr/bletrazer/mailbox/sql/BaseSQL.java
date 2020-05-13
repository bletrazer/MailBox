package fr.bletrazer.mailbox.sql;

import java.util.List;
import java.util.UUID;

import fr.bletrazer.mailbox.DataManager.Data;

public abstract class BaseSQL<T extends Data> {
	private static final SQLConnection sqlConnection = SQLConnection.getInstance();

	protected SQLConnection getSqlConnection() {
		return sqlConnection;
	}

	protected abstract T onCreate(T obj);

	protected abstract T create(T obj);

	protected abstract List<T> createAll(List<T> list);

	protected abstract List<T> onFind(UUID uuid);

	protected abstract List<T> find(UUID uuid);

	protected abstract T onUpdate(Long id, T obj);

	protected abstract T update(Long id, T obj);

	protected abstract List<T> updateAll(List<T> list);

	protected abstract Boolean onDelete(T obj);

	protected abstract Boolean delete(T obj);

	protected abstract Boolean deleteAll(List<T> list);

}