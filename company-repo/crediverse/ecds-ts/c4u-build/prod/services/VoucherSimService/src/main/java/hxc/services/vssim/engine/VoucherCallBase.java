package hxc.services.vssim.engine;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.IVsipRequest;
import hxc.utils.protocol.vsip.IVsipResponse;
import hxc.utils.protocol.vsip.Protocol;

public abstract class VoucherCallBase<Tresp extends IVsipResponse, Treq extends IVsipRequest>
{
	final static Logger logger = LoggerFactory.getLogger(VoucherCallBase.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected IVoucherInfo voucherInfo;
	protected String name;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public VoucherCallBase(IVoucherInfo voucherInfo, String name)
	{
		this.voucherInfo = voucherInfo;
		this.name = name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	protected Tresp exitWith(Tresp response, int responseCode)
	{
		response.setResponseCode(responseCode);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public Tresp execute(Tresp resp, Treq request, String operatorID)
	{
		Date started = new Date();

		logger.info("Request started");

		int responseCode = voucherInfo.getInjectedResponse(name);
		if (responseCode != Protocol.RESPONSECODE_SUCCESS)
		{
			resp.setResponseCode(responseCode);
			return resp;
		}

		Tresp response = exec(request, operatorID);

		if (response.getResponseCode() == Protocol.RESPONSECODE_SUCCESS)
		{
			long ms = new Date().getTime() - started.getTime();
			logger.info("Request completed ({} ms)", ms);
		}
		else
		{
			String text = "??";
			switch (response.getResponseCode())
			{
				case Protocol.RESPONSECODE_SUCCESS:
					text = "Success";
					break;

				case Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST:
					text = "Voucher doesn't exist";
					break;

				case Protocol.RESPONSECODE_VOUCHER_ALREADY_USED:
					text = "Voucher already used";
					break;

				case Protocol.RESPONSECODE_VOUCHER_MISSING_STOLEN:
					text = "Voucher missing or stolen";
					break;

				case Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE:
					text = "Voucher unavaliable";
					break;

				case Protocol.RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER:
					text = "Voucher already used by same Subscriber";
					break;

				case Protocol.RESPONSECODE_VOUCHER_RESERVED_SAME_SUBSCRIBER:
					text = "Voucher already reserved by same Subscriber";
					break;

				case Protocol.RESPONSECODE_VOUCHER_EXPIRED:
					text = "Voucher has Expired";
					break;

				case Protocol.RESPONSECODE_RESERVED:
					text = "Reserved";
					break;

				case Protocol.RESPONSECODE_SUBSCRIBER_ID_MISMATCH:
					text = "Subscriber ID mismatch";
					break;

				case Protocol.RESPONSECODE_VOUCHER_NOT_RESERVED:
					text = "Voucher has not been reserved";
					break;

				case Protocol.RESPONSECODE_TRANSACTION_ID_MISMATCH:
					text = "Transaction ID mismatch";
					break;

				case Protocol.RESPONSECODE_VOUCHER_DAMAGED:
					text = "Voucher Damaged";
					break;

				case Protocol.RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER:
					text = "Voucher reserved for another Supplier";
					break;

				case Protocol.RESPONSECODE_DATABASE_ERROR:
					text = "Database Error";
					break;

				case Protocol.RESPONSECODE_BAD_STATE_TRANSITION:
					text = "Bad State Transition";
					break;

				case Protocol.RESPONSECODE_STATE_CHANGE_LIMITS_EXCEEDED:
					text = "State change limits exceeded";
					break;

				case Protocol.RESPONSECODE_TASK_DOESNT_EXIST:
					text = "Task doesn't exist";
					break;

				case Protocol.RESPONSECODE_CANNOT_DELETE_RUNNING_TASK:
					text = "Cannot delete a running task";
					break;

				case Protocol.RESPONSECODE_MALFORMED_REQUEST:
					text = "Malformed Request";
					break;

				case Protocol.RESPONSECODE_MALFORMED_RESPONSE:
					text = "Malformed Response";
					break;

				case Protocol.RESPONSECODE_NOT_IMPLEMENTED_YET:
					text = "Not implemented yet";
					break;

				default:
					text = "Unknown";
			}

			logger.error("Request Failed with Error Code {} ({})", response.getResponseCode(), text);
		}

		return response;
	}

	public abstract Tresp exec(Treq request, String operatorID);

	protected boolean perform(ScheduledTask<Treq> task) throws Exception
	{
		logger.info("Task started");
		Date started = new Date();

		boolean result = execute(task);

		if (result)
		{
			long ms = new Date().getTime() - started.getTime();
			logger.info("Task completed ({} ms)", ms);
		}
		else
			logger.error("Task Failed");

		return result;
	}

	protected boolean execute(ScheduledTask<Treq> task) throws Exception
	{
		return false;
	}

}
