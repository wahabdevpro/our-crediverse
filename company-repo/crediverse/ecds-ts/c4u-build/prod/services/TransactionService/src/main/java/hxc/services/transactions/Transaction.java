package hxc.services.transactions;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import hxc.connectors.database.IDatabaseConnection;
import hxc.services.logging.LoggingConstants;

public class Transaction<T extends ICdr> implements Closeable, ITransaction
{
	final static Logger logger = LoggerFactory.getLogger(Transaction.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private T cdr;
	private IDatabaseConnection database;
	private Stack<Reversal> reversals = new Stack<Reversal>();
	private ITransactionService transactionService;
	private boolean completed = false;
	private long started = System.currentTimeMillis();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public Transaction(TransactionService transactionService, T cdr, IDatabaseConnection database)
	{
		this.transactionService = transactionService;
		this.cdr = cdr;

		this.database = database;
		try
		{
			if (database != null)
				database.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.error("Failed to turn off autocommit", e);
		}
		MDC.put(LoggingConstants.CONST_LOG_TRANSID, cdr.getTransactionID());

		logger.debug("Transaction Started");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Closeable members
	//
	// /////////////////////////////////
	@Override
	public void close() throws IOException
	{
		try
		{
			// Rollback if Required
			if (!completed)
			{
				logger.warn("Transaction Aborted");

				while (!reversals.isEmpty())
				{
					cdr.setRolledBack(true);
					Reversal reversal = reversals.pop();
					try
					{
						reversal.reverse();
					}
					catch (Exception ex)
					{
						logger.error("Rollback Failed - Follow-up flag set in CDR", ex);
						cdr.setAdditionalInformation("Rollback Failed");
						cdr.setFollowUp(true);
						break;
					}
				}

				if (database != null)
					database.rollback();
			}
			else
			{
				if (database != null)
					database.commit();

				logger.debug("Transaction Completed {} ms", System.currentTimeMillis() - started);
			}

		}
		catch (SQLException e)
		{
			throw new IOException(e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (database != null)
					database.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				throw new IOException(e.getMessage(), e);
			}
			finally
			{
				// Write the CDR
				cdr.Write(transactionService.getFileOutputStream());
				transactionService.setLastCdr(cdr);

				MDC.remove(LoggingConstants.CONST_LOG_TRANSID);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Public Methods
	//
	// /////////////////////////////////
	public void complete()
	{
		completed = true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ITransaction Members
	//
	// /////////////////////////////////

	@Override
	public Date getStartTime()
	{
		return cdr.getStartTime();
	}

	@Override
	public String getTransactionID()
	{
		return cdr.getTransactionID();
	}

	@Override
	public int getResultCode()
	{
		return 0; // cdr.getResultCode();
	}

	@Override
	public void setResultCode(int resultCode)
	{
		// cdr.setResultCode(resultCode);
	}

	@Override
	public void addReversal(Reversal reversal)
	{
		reversals.push(reversal);
	}

	@Override
	public void setLastNotification(String message)
	{
		// cdr.setLastNotification(message);
	}

	public void track(Object origin, String lastAction)
	{
		cdr.setLastActionID(lastAction);
		logger.debug(lastAction);
	}

}
