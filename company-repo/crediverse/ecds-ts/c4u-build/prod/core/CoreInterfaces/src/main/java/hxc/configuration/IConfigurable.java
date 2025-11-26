package hxc.configuration;

import java.sql.SQLException;

import hxc.connectors.database.IDatabaseConnection;

public interface IConfigurable
{
	void save(IDatabaseConnection database, long serialVersionUID) throws SQLException;

	IConfigurable load(IDatabaseConnection databaseConnection, long serialVersionUID) throws SQLException;
}
