package hxc.services.vssim.engine;

import java.util.Date;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.PurgeVouchersCallRequest;
import hxc.utils.protocol.vsip.PurgeVouchersCallResponse;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;
import hxc.utils.protocol.vsip.PurgeVouchersResponse;
import hxc.utils.protocol.vsip.PurgeVouchersSchedulation;

public class PurgeVouchers extends VoucherCallBase<PurgeVouchersResponse, PurgeVouchersRequest>
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
	public PurgeVouchers(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "PurgeVouchers");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static PurgeVouchersCallResponse call(IVoucherInfo voucherInfo, PurgeVouchersCallRequest call, String operatorID)
	{
		PurgeVouchersCallResponse response = new PurgeVouchersCallResponse();
		response.setResponse(new PurgeVouchers(voucherInfo).execute(new PurgeVouchersResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public PurgeVouchersResponse exec(PurgeVouchersRequest request, String operatorID)
	{
		// Create Response
		PurgeVouchersResponse response = new PurgeVouchersResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		ScheduledTask<PurgeVouchersRequest> task = //
		new ScheduledTask<PurgeVouchersRequest>(request, request.getNetworkOperatorId(), operatorID)
		{
			@Override
			public boolean execute() throws Exception
			{
				return perform(this);
			}
		};

		PurgeVouchersSchedulation schedule = request.getSchedulation();
		int taskID;
		if (schedule != null)
			taskID = voucherInfo.scheduleTask(task, schedule.getExecutionTime(), schedule.getRecurrence(), schedule.getRecurrenceValue());
		else
			taskID = voucherInfo.scheduleTask(task, null, null, null);
		if (taskID < 0)
			return exitWith(response, Protocol.RESPONSECODE_DATABASE_ERROR);

		response.setTaskId(taskID);
		response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);

		// Validate Response
		if (response.getResponseCode() != Protocol.RESPONSECODE_SUCCESS)
			return response;
		else if (!response.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_RESPONSE);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	@Override
	protected boolean execute(ScheduledTask<PurgeVouchersRequest> task)
	{
		PurgeVouchersRequest request = task.getRequest();

		Date expiryDate = request.getExpiryDate();
		if (expiryDate == null)
			expiryDate = DateTime.getToday().addDays(-request.getOffset());

		return voucherInfo.purge(request.getNetworkOperatorId(), expiryDate, request.getState(), //
				request.getPurgeVouchers(), request.getOutputVAC());

	}

}
