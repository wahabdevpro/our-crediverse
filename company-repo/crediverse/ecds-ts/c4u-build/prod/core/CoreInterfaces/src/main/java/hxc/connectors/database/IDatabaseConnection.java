package hxc.connectors.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IDatabaseConnection extends AutoCloseable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Select Commands
	//
	// /////////////////////////////////
	public abstract <T> List<T> selectList(Class<T> type, String whereClause, Object... parameters) throws SQLException;

	public abstract <T> T select(Class<T> type, String whereClause, Object... parameters) throws SQLException;

	public abstract <V> V selectScalar(Class<V> type, String selectClause, Object... parameters) throws SQLException;

	public abstract <V> List<V> selectVector(Class<V> type, String selectClause, Object... parameters) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Insert, Update, Upsert and Delete
	//
	// /////////////////////////////////
	public abstract <T> void insert(T instance) throws SQLException;

	public abstract <T> int update(T instance, String... columns) throws SQLException;

	public abstract <T> int update(T instance, String[] columns, Object... values) throws SQLException;

	public abstract <T> boolean upsert(T instance) throws SQLException;

	public abstract <T> boolean delete(T instance) throws SQLException;

	public abstract boolean delete(Class<?> type, String whereClause, Object... parameters) throws SQLException;

	public abstract int executeNonQuery(String sql, Object... parameters) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// DDL Methods
	//
	// /////////////////////////////////
	public abstract boolean databaseExists(String name) throws SQLException;

	public abstract void dropDatabase(String name) throws SQLException;

	public abstract void createDatabase(String name) throws SQLException;

	public abstract boolean tableExists(Class<?> type) throws SQLException;

	public abstract void dropTable(Class<?> type) throws SQLException;

	public abstract void createTable(Class<?> type) throws SQLException;

	public abstract void testCreateTable(Class<?> type) throws SQLException;

	public abstract void updateTable(Class<?> type) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transactional Control
	//
	// /////////////////////////////////
	public abstract boolean isAutoCommit() throws SQLException;;

	public abstract void setAutoCommit(boolean autoCommit) throws SQLException;;

	public abstract void commit() throws SQLException;

	public abstract void rollback() throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Other
	//
	// /////////////////////////////////
	public abstract IDatabase getDatabase();

	public abstract Connection getConnection();

}