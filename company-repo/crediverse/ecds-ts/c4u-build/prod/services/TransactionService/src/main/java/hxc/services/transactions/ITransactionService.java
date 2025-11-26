package hxc.services.transactions;

import java.io.FileOutputStream;

import hxc.connectors.database.IDatabaseConnection;

public interface ITransactionService
{
	public abstract <T extends ICdr> Transaction<?> create(T cdr, IDatabaseConnection database);

	public abstract FileOutputStream getFileOutputStream();

	public abstract ICdr getLastCdr();

	public abstract void setLastCdr(ICdr cdr);

}
