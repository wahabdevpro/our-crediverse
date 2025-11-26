package hxc.services.pin;

import com.concurrent.hxc.ResponseHeader;

import hxc.connectors.database.IDatabaseConnection;
import hxc.services.transactions.CdrBase;

public interface IPinService
{
	public static final String DEFAULT_VARIANT_ID = "DEF";

	public abstract boolean resetPIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn);

	public abstract boolean changePIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn, String oldPIN, String newPIN);

	public abstract boolean validatePIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn, String pin);

	public abstract boolean hasValidPIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn);

}