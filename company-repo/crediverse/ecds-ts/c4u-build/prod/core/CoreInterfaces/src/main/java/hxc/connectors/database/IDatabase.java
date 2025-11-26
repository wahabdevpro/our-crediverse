package hxc.connectors.database;

import java.io.IOException;

public interface IDatabase
{
	public abstract IDatabaseConnection getConnection(String optionalConnectionString) throws IOException;

	public abstract boolean isAvailable(String server);

	public abstract boolean isFit();

	public abstract String backup() throws Exception;

	public abstract boolean restoreFromBackup(String backup);
}