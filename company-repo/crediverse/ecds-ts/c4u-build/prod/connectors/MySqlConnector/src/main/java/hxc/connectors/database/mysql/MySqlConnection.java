package hxc.connectors.database.mysql;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IConnection;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;

public class MySqlConnection implements IConnection, IDatabaseConnection
{
	final static Logger logger = LoggerFactory.getLogger(MySqlConnection.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private MySqlConnector connector;
	private Connection connection;
	private static Object[] questions;
	static
	{
		int maxQuestions = 100;
		questions = new Object[maxQuestions];
		for (int index = 0; index < maxQuestions; index++)
			questions[index] = "?";
	}
	private int connectionHash;

	public int getConnectionHash()
	{
		return connectionHash;
	}

	@Override
	public Connection getConnection()
	{
		return connection;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnection Implementation
	//
	// /////////////////////////////////
	public MySqlConnection(MySqlConnector connector, Connection connection, int connectionHash)
	{
		this.connector = connector;
		this.connection = connection;
		this.connectionHash = connectionHash;
	}

	@Override
	public IDatabase getDatabase()
	{
		return connector;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Select Commands
	//
	// /////////////////////////////////
	@Override
	public <T> List<T> selectList(Class<T> type, String whereClause, Object... parameters) throws SQLException
	{
		// Create Results
		List<T> results = new ArrayList<T>();

		try
		{
			// Prepare the Statement
			TableInfo tableInfo = getTableInfo(type);
			if (!whereClause.toLowerCase().startsWith("select"))
				whereClause = String.format("SELECT * FROM `%s` %s", tableInfo.getTableName(), whereClause);

			// Execute the request
			try (PreparedStatement statement = prepareStatement(whereClause, parameters))
			{
				try (ResultSet resultSet = statement.executeQuery())
				{
					// ?? Optimize
					// Get meta-data
					ResultSetMetaData metadata = resultSet.getMetaData();
					int count = metadata.getColumnCount();
					List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
					for (int columnIndex = 1; columnIndex <= count; columnIndex++)
					{
						String columnName = metadata.getColumnName(columnIndex).toLowerCase();

						ColumnInfo columnInfo = tableInfo.getColumnsByColumnName().get(columnName);
						columns.add(columnInfo);
					}

					while (resultSet.next())
					{
						try
						{
							T result = tableInfo.getClassInfo().newInstance();

							for (int columnIndex = 1; columnIndex <= count; columnIndex++)
							{

								ColumnInfo columnInfo = columns.get(columnIndex - 1);
								if (columnInfo == null)
									continue;
								Object value = resultSet.getObject(columnIndex);
								value = fromMySqlValue(columnInfo.getFieldInfo().getType(), value);
								columnInfo.getFieldInfo().set(result, value);

							}
							results.add(result);

						}
						catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
						{
							throw new SQLException(e.getMessage(), e);
						}

					}
				}

			}
			catch (Exception e)
			{
				logger.error("selectList", e);
				throw e;
			}

		}
		catch (SQLException e)
		{
			logger.error("selectList SQL error", e);
			throw e;
		}

		return results;
	}

	@Override
	public <T> T select(Class<T> type, String whereClause, Object... parameters) throws SQLException
	{
		List<T> results = selectList(type, whereClause, parameters);
		int count = results.size();
		if (count == 1)
			return results.get(0);
		else if (count == 0)
			return null;
		else
			throw new SQLException("More than one record selected");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V selectScalar(Class<V> type, String selectClause, Object... parameters) throws SQLException
	{
		try (PreparedStatement statement = prepareStatement(selectClause, parameters))
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					return (V) fromMySqlValue(type, resultSet.getObject(1));
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> List<V> selectVector(Class<V> type, String selectClause, Object... parameters) throws SQLException
	{
		List<V> results = new ArrayList<V>();
		try (PreparedStatement statement = prepareStatement(selectClause, parameters))
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				while (resultSet.next())
				{
					results.add((V) fromMySqlValue(type, resultSet.getObject(1)));
				}
			}
		}
		return results;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Insert, Update and Delete
	//
	// /////////////////////////////////

	@Override
	public <T> void insert(T instance) throws SQLException
	{
		TableInfo tableInfo = getTableInfo(instance.getClass());

		// Create INSERT DDL if it doesn't exist
		String insertSql = tableInfo.getDDL("insert");
		if (insertSql == null || insertSql.length() == 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO `");
			sql.append(tableInfo.getTableName());
			sql.append("` (");
			boolean first1 = true;
			int count = 0;
			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (column.isReadonly())
					continue;
				count++;
				if (first1)
				{
					first1 = false;
					sql.append("\n`");
					sql.append(column.getName());
					sql.append('`');
				}
				else
				{
					sql.append("\n,`");
					sql.append(column.getName());
					sql.append('`');
				}
			}
			sql.append("\n) VALUES ( ?");
			for (int index = 1; index < count; index++)
				sql.append(", ?");
			sql.append(")");
			insertSql = sql.toString();
			tableInfo.settDDL("insert", insertSql);
		}

		// Create Value List
		List<Object> values = new ArrayList<Object>();
		for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
		{
			if (column.isReadonly())
				continue;
			try
			{
				values.add(column.getFieldInfo().get(instance));
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new SQLException(e.getMessage(), e);
			}
		}

		// Execute the Insert
		executeNonQuery(insertSql, values.toArray());

	}

	@Override
	public <T> int update(T instance, String... columns) throws SQLException
	{
		TableInfo tableInfo = getTableInfo(instance.getClass());

		List<String> columnList = new ArrayList<String>();
		boolean onlySome = false;
		if (columns != null && columns.length > 0)
		{
			for (String col : columns)
			{
				columnList.add(col.toLowerCase());
			}
			onlySome = true;
		}

		// Create UPDATE DDL if it doesn't exist
		String updateSql = tableInfo.getDDL("update");
		if (onlySome || updateSql == null || updateSql.length() == 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE `");
			sql.append(tableInfo.getTableName());
			sql.append("`\nSET ");
			boolean first1 = true;
			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (column.isReadonly() || column.isPrimaryKey())
					continue;

				if (onlySome && !columnList.contains(column.getName().toLowerCase()))
					continue;

				if (first1)
					first1 = false;
				else
					sql.append("\n,");
				sql.append("`");
				sql.append(column.getName());
				sql.append("` = ?");
			}

			boolean first2 = true;
			sql.append("\nWHERE ");

			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (!column.isPrimaryKey())
					continue;

				if (first2)
					first2 = false;
				else
					sql.append("\nAND ");
				sql.append("`");
				sql.append(column.getName());
				sql.append("` = ?");
			}
			sql.append(';');

			updateSql = sql.toString();

			if (!onlySome)
				tableInfo.settDDL("update", updateSql);
		}

		// Create Value List
		List<Object> values = new ArrayList<Object>();
		try
		{
			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (column.isReadonly() || column.isPrimaryKey())
					continue;

				if (onlySome && !columnList.contains(column.getName().toLowerCase()))
					continue;

				values.add(column.getFieldInfo().get(instance));
			}

			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (!column.isPrimaryKey())
					continue;
				values.add(column.getFieldInfo().get(instance));
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new SQLException(e.getMessage(), e);
		}

		// Execute the Update
		return executeNonQuery(updateSql, values.toArray());

	}

	@Override
	public <T> int update(T instance, String[] columns, Object... values2) throws SQLException
	{
		TableInfo tableInfo = getTableInfo(instance.getClass());

		// Create UPDATE DDL
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE `");
		sql.append(tableInfo.getTableName());
		sql.append("`\nSET ");
		boolean first1 = true;
		for (String column : columns)
		{
			if (first1)
				first1 = false;
			else
				sql.append("\n,");
			sql.append("`");
			sql.append(column);
			sql.append("` = ?");
		}

		boolean first2 = true;
		sql.append("\nWHERE ");

		for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
		{
			if (!column.isPrimaryKey())
				continue;

			if (first2)
				first2 = false;
			else
				sql.append("\nAND ");
			sql.append("`");
			sql.append(column.getName());
			sql.append("` = ?");
		}
		sql.append(';');

		String updateSql = sql.toString();

		// Create Value List
		List<Object> values = new ArrayList<Object>();
		try
		{
			for (Object value : values2)
			{
				values.add(value);
			}

			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (!column.isPrimaryKey())
					continue;
				values.add(column.getFieldInfo().get(instance));
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new SQLException(e.getMessage(), e);
		}

		// Execute the Update
		return executeNonQuery(updateSql, values.toArray());
	}

	@Override
	public <T> boolean upsert(T instance) throws SQLException
	{
		if (update(instance) == 0)
		{
			insert(instance);
			return true;
		}
		return false;
	}

	@Override
	public <T> boolean delete(T instance) throws SQLException
	{
		TableInfo tableInfo = getTableInfo(instance.getClass());

		// Create DELETE DDL if it doesn't exist
		String deleteSql = tableInfo.getDDL("delete");
		if (deleteSql == null || deleteSql.length() == 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM `");
			sql.append(tableInfo.getTableName());
			sql.append("`\nWHERE ");
			boolean first1 = true;
			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (!column.isPrimaryKey())
					continue;
				if (first1)
					first1 = false;
				else
					sql.append("\nAND ");
				sql.append("`");
				sql.append(column.getName());
				sql.append("` = ?");
			}
			sql.append(';');

			deleteSql = sql.toString();
			tableInfo.settDDL("delete", deleteSql);
		}

		// Create Value List
		List<Object> values = new ArrayList<Object>();
		try
		{

			for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
			{
				if (!column.isPrimaryKey())
					continue;
				values.add(column.getFieldInfo().get(instance));
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new SQLException(e.getMessage(), e);
		}

		// Execute the Update
		return executeNonQuery(deleteSql, values.toArray()) > 0;

	}

	@Override
	public boolean delete(Class<?> type, String whereClause, Object... parameters) throws SQLException
	{
		TableInfo tableInfo = getTableInfo(type);

		String sql = String.format("delete from `%s` %s", tableInfo.getTableName(), whereClause);
		return executeNonQuery(sql, parameters) > 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// DDL Methods
	//
	// /////////////////////////////////

	@Override
	public boolean databaseExists(String name) throws SQLException
	{
		String sql = "SELECT count(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = %s LIMIT 1;";
		long result = selectScalar(long.class, sql, name);
		return result == 1;
	}

	@Override
	public void dropDatabase(String name) throws SQLException
	{
		executeNonQuery("drop database " + name);
	}

	@Override
	public void createDatabase(String name) throws SQLException
	{
		executeNonQuery("CREATE DATABASE `" + name + "`  CHARACTER SET utf8;");
	}

	@Override
	public boolean tableExists(Class<?> type) throws SQLException
	{
		TableInfo tableInfo = TableInfo.get(type);
		String sql = "SELECT count(*) FROM information_schema.tables WHERE table_schema = %s AND table_name = %s LIMIT 1;";
		long result = selectScalar(long.class, sql, connection.getCatalog(), tableInfo.getTableName());
		return result == 1;
	}

	@Override
	public void dropTable(Class<?> type) throws SQLException
	{
		if (!tableExists(type))
			return;
		TableInfo tableInfo = TableInfo.get(type);
		executeNonQuery(String.format("drop table `%s`.`%s`;", connection.getCatalog(), tableInfo.getTableName()));
	}

	@Override
	public void createTable(Class<?> type) throws SQLException
	{
		TableInfo tableInfo = TableInfo.get(type);

		// Create Statement
		StringBuilder sql = new StringBuilder(1000);
		sql.append(String.format("CREATE TABLE `%s`.`%s` (\n", connection.getCatalog(), tableInfo.getTableName()));

		// Columns
		boolean first1 = true;
		for (ColumnInfo column : tableInfo.getColumnsByColumnName().values())
		{
			if (first1)
				first1 = false;
			else
				sql.append(",\n");
			sql.append(columnDDL(column));
		}
		sql.append(",\n`ts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

		// Primary Key
		if (tableInfo.getPrimaryKeyColumns().size() > 0)
		{
			sql.append(",\nPRIMARY KEY (");
			boolean first2 = true;
			for (ColumnInfo column : tableInfo.getPrimaryKeyColumns())
			{
				if (first2)
					first2 = false;
				else
					sql.append(", ");
				sql.append(String.format("`%s`", column.getName()));
			}
			sql.append(")");
		}

		// Unique Index
		String uniqueIndex = tableInfo.getUniqueIndex();
		if (uniqueIndex != null && uniqueIndex.length() > 0)
		{
			String[] parts = uniqueIndex.split(",");
			sql.append(",\nUNIQUE INDEX `");
			sql.append(parts[0]);
			sql.append("_UNIQUE` (");
			boolean first3 = true;
			for (String part : parts)
			{
				if (first3)
					first3 = false;
				else
					sql.append(", ");
				sql.append(part);
			}
			sql.append(')');
		}

		// The end
		sql.append(");");
		executeNonQuery(sql.toString());
	}

	@Override
	public void testCreateTable(Class<?> type) throws SQLException
	{
		@SuppressWarnings("unused")
		TableInfo tableInfo = getTableInfo(type);
	}

	@Override
	public void updateTable(Class<?> type) throws SQLException
	{
		@SuppressWarnings("unused")
		TableInfo tableInfo = TableInfo.get(type);
		throw new SQLException("UpdateTable not implemented yet");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transactional Control
	//
	// /////////////////////////////////

	@Override
	public boolean isAutoCommit() throws SQLException
	{
		return connection.getAutoCommit();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		connection.setAutoCommit(autoCommit);
	}

	@Override
	public void commit() throws SQLException
	{
		connection.commit();
	}

	@Override
	public void rollback() throws SQLException
	{
		connection.rollback();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	@Override
	public int executeNonQuery(String sql, Object... parameters) throws SQLException
	{
		try (PreparedStatement statement = prepareStatement(sql, parameters))
		{
			boolean hasResultSet = statement.execute();
			return hasResultSet ? 1 : statement.getUpdateCount();
		}
	}

	// private ResultSet executeResultSet(String sql, Object... parameters) throws SQLException
	// {
	// PreparedStatement statement = prepareStatement(sql, parameters);
	// return statement.executeQuery();
	// }

	private PreparedStatement prepareStatement(String sql, Object... parameters) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement(String.format(sql, questions));
		for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++)
		{
			Object parameter = parameters[parameterIndex - 1];
			statement.setObject(parameterIndex, toMySqlValue(parameter));
		}
		return statement;
	}

	private String columnDDL(ColumnInfo columnInfo) throws SQLException
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nil = columnInfo.isNullable() ? "NULL" : "NOT NULL";
		Class<?> type = columnInfo.getFieldInfo().getField().getType();
		String typeName = type.isEnum() ? "int" : type.getName();
		Object defaultValue = columnInfo.getDefaultValue();
		boolean hasDefault = defaultValue != null;
		String defaultString = "";

		switch (typeName)
		{
			case "java.lang.String":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%s'", defaultValue);
				if (columnInfo.getMaxLength() >= 1024)
					return String.format("`%1$s` TEXT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);
				else
					return String.format("`%1$s` VARCHAR(%2$d) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "byte":
			case "java.lang.Byte":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%d'", defaultValue);
				return String.format("`%1$s` TINYINT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "short":
			case "java.lang.Short":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%d'", defaultValue);
				return String.format("`%1$s` SMALLINT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "int":
			case "java.lang.Integer":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%d'", defaultValue);
				return String.format("`%1$s` INT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "long":
			case "java.lang.Long":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%d'", defaultValue);
				return String.format("`%1$s` BIGINT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "java.math.BigDecimal":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%s'", ((BigDecimal) defaultValue).toPlainString());
				return String.format("`%1$s` DECIMAL(20,4) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "java.util.Date":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%s'", dateFormat.format((Date) defaultValue));
				return String.format("`%1$s` DATETIME %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "boolean":
			case "java.lang.Boolean":
				if (hasDefault)
					defaultString = String.format(" DEFAULT %s", (boolean) defaultValue ? "b'1'" : "b'0'");
				return String.format("`%1$s` BIT(1) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "char":
			case "java.lang.Character":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%c'", defaultValue);
				return String.format("`%1$s` CHAR(1) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "double":
			case "java.lang.Double":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%f'", defaultValue);
				return String.format("`%1$s` DOUBLE %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "float":
			case "java.lang.Float":
				if (hasDefault)
					defaultString = String.format(" DEFAULT '%f'", defaultValue);
				return String.format("`%1$s` FLOAT %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "[B":
				if (hasDefault)
					defaultString = ""; // String.format("", defaultValue);
				return String.format("`%1$s` VARBINARY(%2$d) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			case "java.util.UUID":
				if (hasDefault)
					defaultString = ""; // String.format("", defaultValue);
				return String.format("`%1$s` BINARY(16) %3$s%4$s", columnInfo.getName(), columnInfo.getMaxLength(), nil, defaultString);

			default:
				throw new SQLException(typeName);
		}
	}

	private TableInfo getTableInfo(Class<?> type) throws SQLException
	{
		TableInfo result = TableInfo.get(type);
		if (!result.isExistanceChecked())
		{
			if (!tableExists(type))
				createTable(type);
			result.setExistanceChecked(true);
		}

		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Coercing Values
	//
	// /////////////////////////////////

	private Object toMySqlValue(Object value)
	{
		// Null
		if (value == null)
			return value;

		// char
		Class<?> sourceType = value.getClass();
		if (sourceType == char.class || sourceType == Character.class)
			return "" + (char) value;

		// Enum
		if (sourceType.isEnum())
			return ((Enum<?>) value).ordinal();

		// UUID
		if (sourceType == java.util.UUID.class)
		{
			UUID uuid = (UUID) value;
			ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
			bb.putLong(uuid.getMostSignificantBits());
			bb.putLong(uuid.getLeastSignificantBits());
			return bb.array();
		}

		return value;
	}

	private Object fromMySqlValue(Class<?> targetType, Object mySqlValue)
	{
		// Null
		if (mySqlValue == null)
			return null;

		// Enums
		Class<?> sourceType = mySqlValue.getClass();
		if (targetType.isEnum())
			return targetType.getEnumConstants()[(int) mySqlValue];

		if (targetType == byte.class || targetType == Byte.class)
			return (byte) (int) mySqlValue;

		if (targetType == short.class || targetType == Short.class)
			return (short) (int) mySqlValue;

		if (sourceType == java.sql.Timestamp.class)
		{
			long time = ((java.sql.Timestamp) mySqlValue).getTime();
			return new java.util.Date(time);
		}
		else if (sourceType == java.sql.Date.class)
		{
			long time = ((java.sql.Date) mySqlValue).getTime();
			return new java.util.Date(time);
		}

		if (targetType == java.util.UUID.class)
		{
			ByteBuffer bb = ByteBuffer.wrap((byte[]) mySqlValue);
			long msb = bb.getLong();
			long lsb = bb.getLong();
			return new UUID(msb, lsb);
		}

		if (targetType == char.class || targetType == Character.class)
		{
			String text = (String) mySqlValue;
			if (text != null && text.length() > 0)
				return text.charAt(0);
			else
				return '\0';
		}

		return mySqlValue;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Closeable Implementation
	//
	// /////////////////////////////////
	@Override
	public void close() throws IOException
	{
		try
		{
			connector.returnConnection(this);
		}
		catch (SQLException e)
		{
			logger.warn("Exception closing connection", e);
			throw new IOException(e.getMessage(), e);
		}

	}

}
