package hxc.services.vssim.engine;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.GenerateVoucherCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherCallResponse;
import hxc.utils.protocol.vsip.GenerateVoucherRequest;
import hxc.utils.protocol.vsip.GenerateVoucherResponse;
import hxc.utils.protocol.vsip.GenerateVoucherSchedulation;
import hxc.utils.protocol.vsip.Protocol;

public class GenerateVoucher extends VoucherCallBase<GenerateVoucherResponse, GenerateVoucherRequest>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static AtomicInteger nextBatchID = new AtomicInteger(10000);

	private static final int MAX_UNIQUE_ATTEMPTS = 1000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GenerateVoucher(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GenerateVoucher");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GenerateVoucherCallResponse call(IVoucherInfo voucherInfo, GenerateVoucherCallRequest call, String operatorID)
	{
		GenerateVoucherCallResponse response = new GenerateVoucherCallResponse();
		response.setResponse(new GenerateVoucher(voucherInfo).execute(new GenerateVoucherResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherResponse exec(GenerateVoucherRequest request, String operatorID)
	{
		// Create Response
		GenerateVoucherResponse response = new GenerateVoucherResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_REQUEST);
			return response;
		}

		// Do Simulation here...
		ScheduledTask<GenerateVoucherRequest> task = //
		new ScheduledTask<GenerateVoucherRequest>(request, request.getNetworkOperatorId(), operatorID)
		{
			@Override
			public boolean execute() throws Exception
			{
				return perform(this);
			}

		};
		GenerateVoucherSchedulation schedule = request.getSchedulation();
		int taskID;
		if (schedule != null)
			taskID = voucherInfo.scheduleTask(task, schedule.getExecutionTime(), null, null);
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
	private static final Pattern lastDigitsPattern = Pattern.compile("^([0-9]+).*");

	@Override
	protected boolean execute(ScheduledTask<GenerateVoucherRequest> generateVoucherTask) throws Exception
	{
		// Get the Last Digits of the First Serial Number
		GenerateVoucherRequest request = generateVoucherTask.getRequest();
		String serialNumber = request.getSerialNumber();
		String lastDigits = new StringBuilder(serialNumber).reverse().toString();
		Matcher matcher = lastDigitsPattern.matcher(lastDigits);
		if (!matcher.matches())
		{
			generateVoucherTask.setAdditionalInfo("No Trailing Digits");
			return false;
		}
		lastDigits = new StringBuilder(matcher.group(1)).reverse().toString();
		int length = lastDigits.length();
		String prefix = serialNumber.substring(0, serialNumber.length() - length);
		long nextNumber = Long.parseLong(lastDigits);
		String serialFormat = String.format("%%0%dd", length);

		// Determine the size and format of the activation code
		String activationFormat = String.format("%%0%dd", request.getActivationCodeLength());
		long maxActivationCode = 1;
		for (int index = 0; index < request.getActivationCodeLength(); index++)
		{
			maxActivationCode *= 10;
		}
		Random random = new Random();

		// Calculate a BatchID
		String batchId = Integer.toString(nextBatchID.getAndIncrement());

		Date timestamp = new Date();

		// Generate Vouchers
		for (int index = 0; index < request.getNumberOfVouchers(); index++)
		{
			Voucher voucher = new Voucher();

			// Create Serial Number
			String newSerialNumber = String.format(serialFormat, nextNumber + index);
			if (newSerialNumber.length() > length)
			{
				generateVoucherTask.setAdditionalInfo("Overflow");
				return false;
			}
			voucher.setSerialNumber(prefix + newSerialNumber);

			// Set Other Fields
			voucher.setCurrency(request.getCurrency());
			voucher.setValue(request.getValue());
			voucher.setVoucherGroup(request.getVoucherGroup());
			voucher.setExpiryDate(request.getExpiryDate());
			voucher.setAgent(request.getAgent());
			voucher.setExtensionText1(request.getExtensionText1());
			voucher.setExtensionText2(request.getExtensionText2());
			voucher.setExtensionText3(request.getExtensionText3());
			voucher.setNetworkOperatorId(request.getNetworkOperatorId());
			voucher.setBatchId(batchId);
			voucher.setNewState(Protocol.STATE_AVAILABLE, null, null, timestamp, null);

			// Attempt to add Voucher with unique Activation Code
			boolean success = false;
			for (int attempt = 0; attempt < MAX_UNIQUE_ATTEMPTS; attempt++)
			{
				// Generate a new Activation Code
				long activationCode = random.nextLong() % maxActivationCode;
				if (activationCode < 0)
					activationCode = -activationCode;
				voucher.setActivationCode(String.format(activationFormat, activationCode));

				int result = voucherInfo.addVoucher(voucher);
				if (result == IVoucherInfo.ADD_RESULT_OK)
				{
					success = true;
					break;
				}
				else if (result == IVoucherInfo.ADD_RESULT_DUPLICATE_ACTIVATION)
					continue;
				else
				{
					generateVoucherTask.setAdditionalInfo("Duplicate Serial Number");
					return false;
				}
			}

			// Unable to generate Unique Activation Code
			if (!success)
			{
				generateVoucherTask.setAdditionalInfo("No Unique Activation Code");
				return false;
			}

		}

		return true;
	}

}
