package hxc.services.vssim.engine;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.ChangeVoucherStateCallRequest;
import hxc.utils.protocol.vsip.ChangeVoucherStateCallResponse;
import hxc.utils.protocol.vsip.ChangeVoucherStateRequest;
import hxc.utils.protocol.vsip.ChangeVoucherStateResponse;
import hxc.utils.protocol.vsip.ChangeVoucherStateSchedulation;
import hxc.utils.protocol.vsip.Protocol;

public class ChangeVoucherState extends VoucherCallBase<ChangeVoucherStateResponse, ChangeVoucherStateRequest>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ChangeVoucherState(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "ChangeVoucherState");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static ChangeVoucherStateCallResponse call(IVoucherInfo voucherInfo, ChangeVoucherStateCallRequest call, String operatorID)
	{
		ChangeVoucherStateCallResponse response = new ChangeVoucherStateCallResponse();
		response.setResponse(new ChangeVoucherState(voucherInfo).execute(new ChangeVoucherStateResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public ChangeVoucherStateResponse exec(ChangeVoucherStateRequest request, String operatorID)
	{
		// Create Response
		ChangeVoucherStateResponse response = new ChangeVoucherStateResponse();

		// Set Lower Case States
		if (request.getNewState() != null)
			request.setNewState(request.getNewState().toLowerCase());

		if (request.getOldState() != null)
			request.setOldState(request.getOldState().toLowerCase());

		// Validate Request
		if (!request.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_REQUEST);
			return response;
		}

		// Validate the State Change
		if (request.getOldState() != null && !validateStateChange(request.getOldState(), request.getNewState()))
		{
			return exitWith(response, Protocol.RESPONSECODE_BAD_STATE_TRANSITION);
		}

		// Create the task
		ScheduledTask<ChangeVoucherStateRequest> task = //
		new ScheduledTask<ChangeVoucherStateRequest>(request, request.getNetworkOperatorId(), operatorID)
		{
			@Override
			public boolean execute() throws Exception
			{
				return perform(this);
			}
		};

		ChangeVoucherStateSchedulation schedule = request.getSchedulation();
		int taskID;

		// Schedule task
		if (schedule != null)
			taskID = voucherInfo.scheduleTask(task, schedule.getExecutionTime(), null, null);
		else
			taskID = voucherInfo.scheduleTask(task, null, null, null);

		// Validate task ID
		if (taskID < 0)
			return exitWith(response, Protocol.RESPONSECODE_DATABASE_ERROR);

		response.setTaskId(taskID);
		response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);

		// Validate Response
		if (response.getResponseCode() != Protocol.RESPONSECODE_SUCCESS)
			return response;
		else if (!response.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_RESPONSE);
			return response;
		}

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	@Override
	protected boolean execute(ScheduledTask<ChangeVoucherStateRequest> task)
	{
		ChangeVoucherStateRequest request = task.getRequest();

		// Check if multiple serial numbers
		if (request.getSerialNumberFirst() != null && request.getSerialNumberLast() != null)
		{
			try
			{
				String lastDigitsSerialNumberFirst = lastDigitsSerialNumber(request.getSerialNumberFirst());
				int length = lastDigitsSerialNumberFirst.length();
				String prefix = request.getSerialNumberFirst().substring(0, request.getSerialNumberFirst().length() - length);

				long serialNumberFirst = Long.parseLong(lastDigitsSerialNumberFirst);
				long serialNumberLast = Long.parseLong(lastDigitsSerialNumber(request.getSerialNumberLast()));

				// Loop through the serial numbers
				for (long serialNumber = serialNumberFirst; serialNumber < serialNumberLast + 1; serialNumber++)
				{
					// Get Voucher
					Voucher voucher = super.voucherInfo.getVoucher(String.format("%s%s", prefix, Long.toString(serialNumber)), request.getActivationCode(), request.getNetworkOperatorId());
					if (voucher == null)
						continue;

					// Set the new state
					voucher.setNewState(request.getNewState(), task.getOperatorId(), null, new Date(), null);
				}

				return true;
			}
			catch (Exception e)
			{
				return false;
			}

		}

		// Get Voucher
		Voucher voucher = super.voucherInfo.getVoucher(request.getSerialNumber(), request.getActivationCode(), request.getNetworkOperatorId());
		if (voucher == null)
			return false;

		// Change the state
		return voucher.setNewState(request.getNewState(), null, null, new Date(), null) != null;
	}

	private boolean validateStateChange(String oldState, String newState)
	{
		return ((oldState.equals(Protocol.STATE_UNAVAILABLE) && (newState.equals(Protocol.STATE_AVAILABLE) || newState.equals(Protocol.STATE_DAMAGED) || newState.equals(Protocol.STATE_STOLEN)))
				|| ((oldState.equals(Protocol.STATE_AVAILABLE) && (newState.equals(Protocol.STATE_UNAVAILABLE) || newState.equals(Protocol.STATE_DAMAGED) || newState.equals(Protocol.STATE_STOLEN))))
				|| ((oldState.equals(Protocol.STATE_PENDING) && (newState.equals(Protocol.STATE_AVAILABLE) || newState.equals(Protocol.STATE_USED)))) || ((oldState.equals(Protocol.STATE_STOLEN) && (newState
				.equals(Protocol.STATE_UNAVAILABLE) || newState.equals(Protocol.STATE_AVAILABLE)))));
	}

	private static final Pattern lastDigitsPattern = Pattern.compile("^([0-9]+).*");

	private String lastDigitsSerialNumber(String serialNumber)
	{
		String lastDigits = new StringBuilder(serialNumber).reverse().toString();
		Matcher matcher = lastDigitsPattern.matcher(lastDigits);
		if (!matcher.matches())
		{
			return null;
		}
		lastDigits = new StringBuilder(matcher.group(1)).reverse().toString();
		return lastDigits;
	}
}
