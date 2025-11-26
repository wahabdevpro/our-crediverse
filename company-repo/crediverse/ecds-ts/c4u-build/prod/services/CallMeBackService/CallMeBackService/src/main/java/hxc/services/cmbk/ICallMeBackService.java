package hxc.services.cmbk;

import hxc.connectors.database.IDatabaseConnection;
import hxc.services.transactions.CdrBase;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ProcessResponse;

public interface ICallMeBackService
{
	public static final String DEFAULT_VARIANT_ID = "LCL_ONNET";
	public static final int DEFAULT_SERVICE_CLASS_ID = 0;

	public abstract boolean processCMBK(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String subscriberNumber, String recipientNumber);

}