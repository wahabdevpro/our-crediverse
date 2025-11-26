package hxc.services.vssim;

import java.util.Date;
import java.util.List;

import hxc.services.vssim.model.ScheduledTask;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.IValidationContext;
import hxc.utils.protocol.vsip.IVsipRequest;
import hxc.utils.protocol.vsip.Recurrence;

public interface IVoucherInfo extends IValidationContext
{
	public abstract <TReq extends IVsipRequest> int scheduleTask(ScheduledTask<TReq> task, //
			Date executionTime, Recurrence recurrence, Integer recurrenceValue);

	// Results returned by addVoucher
	public static int ADD_RESULT_OK = 0;
	public static int ADD_RESULT_DUPLICATE_SERIAL = 1;
	public static int ADD_RESULT_DUPLICATE_ACTIVATION = 2;

	public abstract int addVoucher(Voucher voucher);

	public abstract Voucher getVoucher(String serialNumber, String activationCode, String networkOperatorId);

	public abstract boolean purge(String networkOperatorId, Date expiryDate, String state, Boolean purgeVouchers, Boolean outputVAC);

	public abstract <TReq extends IVsipRequest> List<ScheduledTask<TReq>> getTaskInfo(Integer taskId, Class<TReq> cls);

	public abstract <TReq extends IVsipRequest> int deleteTask(int taskId, String networkOperatorId, Class<TReq> cls);

	public abstract int getInjectedResponse(String methodName);

}
