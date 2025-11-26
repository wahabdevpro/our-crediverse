package hxc.services.vssim;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.servicebus.IServiceBus;
import hxc.services.vssim.engine.ChangeVoucherState;
import hxc.services.vssim.engine.DeleteChangeVoucherStateTask;
import hxc.services.vssim.engine.DeleteGenerateVoucherTask;
import hxc.services.vssim.engine.DeleteLoadVoucherBatchTask;
import hxc.services.vssim.engine.DeletePurgeVoucherTask;
import hxc.services.vssim.engine.DeleteVoucherDetailsReportTask;
import hxc.services.vssim.engine.DeleteVoucherDistributionReportTask;
import hxc.services.vssim.engine.DeleteVoucherUsageReportTask;
import hxc.services.vssim.engine.EndReservation;
import hxc.services.vssim.engine.GenerateVoucher;
import hxc.services.vssim.engine.GenerateVoucherDetailsReport;
import hxc.services.vssim.engine.GenerateVoucherDistributionReport;
import hxc.services.vssim.engine.GenerateVoucherUsageReport;
import hxc.services.vssim.engine.GetChangeVoucherStateTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherDetailsReportTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherDistributionReportTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherUsageReportTaskInfo;
import hxc.services.vssim.engine.GetLoadVoucherBatchFileTaskInfo;
import hxc.services.vssim.engine.GetPurgeVouchersTaskInfo;
import hxc.services.vssim.engine.GetVoucherBatchFilesList;
import hxc.services.vssim.engine.GetVoucherDetails;
import hxc.services.vssim.engine.GetVoucherHistory;
import hxc.services.vssim.engine.InjectedResponse;
import hxc.services.vssim.engine.LoadVoucherBatchFile;
import hxc.services.vssim.engine.LoadVoucherCheck;
import hxc.services.vssim.engine.PurgeVouchers;
import hxc.services.vssim.engine.ReserveVoucher;
import hxc.services.vssim.engine.UpdateVoucherState;
import hxc.services.vssim.model.ScheduledTask;
import hxc.services.vssim.model.Voucher;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.vsip.ChangeVoucherStateRequest;
import hxc.utils.protocol.vsip.ChangeVoucherStateResponse;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskRequest;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskResponse;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskRequest;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskResponse;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskRequest;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskResponse;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskRequest;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskResponse;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskResponse;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskResponse;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskResponse;
import hxc.utils.protocol.vsip.EndReservationRequest;
import hxc.utils.protocol.vsip.EndReservationResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportResponse;
import hxc.utils.protocol.vsip.GenerateVoucherRequest;
import hxc.utils.protocol.vsip.GenerateVoucherResponse;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportResponse;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoRequest;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoResponse;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoRequest;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoResponse;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListRequest;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListResponse;
import hxc.utils.protocol.vsip.GetVoucherDetailsRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryResponse;
import hxc.utils.protocol.vsip.IValidationContext;
import hxc.utils.protocol.vsip.IVsipRequest;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileRequest;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileResponse;
import hxc.utils.protocol.vsip.LoadVoucherCheckRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckResponse;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;
import hxc.utils.protocol.vsip.PurgeVouchersResponse;
import hxc.utils.protocol.vsip.Recurrence;
import hxc.utils.protocol.vsip.ReserveVoucherRequest;
import hxc.utils.protocol.vsip.ReserveVoucherResponse;
import hxc.utils.protocol.vsip.UpdateVoucherStateRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateResponse;
import hxc.utils.protocol.vsip.VsipCalls;

@WebService(endpointInterface = "hxc.services.vssim.IVoucherSim")
public class VoucherSim implements IVoucherSim, IVoucherInfo
{
	final static Logger logger = LoggerFactory.getLogger(VoucherSim.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValidationContext context;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private static AtomicInteger nextTaskNumber = new AtomicInteger(1000);
	private ConcurrentHashMap<Integer, ScheduledTask<?>> taskMap = new ConcurrentHashMap<Integer, ScheduledTask<?>>();
	private ConcurrentHashMap<String, Voucher> serialMap = new ConcurrentHashMap<String, Voucher>();
	private ConcurrentHashMap<String, Voucher> activationMap = new ConcurrentHashMap<String, Voucher>();
	private Map<String, InjectedResponse> injectedResponses = new HashMap<String, InjectedResponse>();

	private static final long MS_PER_DAY = 24 * 60 * 60 * 1000;
	private static final int CLEANUP_INTERVAL_S = 60;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public VoucherSim(IServiceBus esb, IValidationContext context, int maxScheduledTasks)
	{
		this.context = context;
		this.scheduledThreadPool = new ScheduledThreadPoolExecutor(maxScheduledTasks, new MyThreadFactory());

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				expireReservations();
			}
		}, CLEANUP_INTERVAL_S, CLEANUP_INTERVAL_S, TimeUnit.SECONDS);
	}

	private class MyThreadFactory implements ThreadFactory
	{
		private int num = 0;

		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "VsipSimThreadPool-" + num++);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IVoucherSim - Housekeeping
	//
	// /////////////////////////////////

	@Override
	public int ping(int seq)
	{
		return seq + 1;
	}

	@Override
	public void reset()
	{
		for (ScheduledTask<?> task : taskMap.values())
		{
			ScheduledFuture<?> future = task.getFuture();
			if (future != null && !future.isDone())
				future.cancel(true);
		}
		taskMap.clear();
		serialMap.clear();
		activationMap.clear();
		nextTaskNumber = new AtomicInteger(1000);
	}

	@Override
	public void injectSelectiveResponse(VsipCalls vsipCall, int responseCode, int skipCount, int failCount)
	{
		InjectedResponse response = new InjectedResponse(vsipCall, responseCode, skipCount, failCount);
		injectedResponses.put(vsipCall.toString().toLowerCase(), response);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IVoucherSim - VSIP
	//
	// /////////////////////////////////
	@Override
	public GetVoucherDetailsResponse getVoucherDetails(GetVoucherDetailsRequest request)
	{
		return new GetVoucherDetails(this).execute(new GetVoucherDetailsResponse(), request, context.getOperatorID());
	}

	@Override
	public GetVoucherHistoryResponse getVoucherHistory(GetVoucherHistoryRequest request)
	{
		return new GetVoucherHistory(this).execute(new GetVoucherHistoryResponse(), request, context.getOperatorID());
	}

	@Override
	public UpdateVoucherStateResponse updateVoucherState(UpdateVoucherStateRequest request)
	{
		return new UpdateVoucherState(this).execute(new UpdateVoucherStateResponse(), request, context.getOperatorID());
	}

	@Override
	public LoadVoucherCheckResponse loadVoucherCheck(LoadVoucherCheckRequest request)
	{
		return new LoadVoucherCheck(this).execute(new LoadVoucherCheckResponse(), request, context.getOperatorID());
	}

	@Override
	public GenerateVoucherResponse generateVoucher(GenerateVoucherRequest request)
	{
		return new GenerateVoucher(this).execute(new GenerateVoucherResponse(), request, context.getOperatorID());
	}

	@Override
	public GetGenerateVoucherTaskInfoResponse getGenerateVoucherTaskInfo(GetGenerateVoucherTaskInfoRequest request)
	{
		return new GetGenerateVoucherTaskInfo(this).execute(new GetGenerateVoucherTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public LoadVoucherBatchFileResponse loadVoucherBatchFile(LoadVoucherBatchFileRequest request)
	{
		return new LoadVoucherBatchFile(this).execute(new LoadVoucherBatchFileResponse(), request, context.getOperatorID());
	}

	@Override
	public GetLoadVoucherBatchFileTaskInfoResponse getLoadVoucherBatchFileTaskInfo(GetLoadVoucherBatchFileTaskInfoRequest request)
	{
		return new GetLoadVoucherBatchFileTaskInfo(this).execute(new GetLoadVoucherBatchFileTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public GetVoucherBatchFilesListResponse getVoucherBatchFilesList(GetVoucherBatchFilesListRequest request)
	{
		return new GetVoucherBatchFilesList(this).execute(new GetVoucherBatchFilesListResponse(), request, context.getOperatorID());
	}

	@Override
	public ChangeVoucherStateResponse changeVoucherState(ChangeVoucherStateRequest request)
	{
		return new ChangeVoucherState(this).execute(new ChangeVoucherStateResponse(), request, context.getOperatorID());
	}

	@Override
	public GetChangeVoucherStateTaskInfoResponse getChangeVoucherStateTaskInfo(GetChangeVoucherStateTaskInfoRequest request)
	{
		return new GetChangeVoucherStateTaskInfo(this).execute(new GetChangeVoucherStateTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public PurgeVouchersResponse purgeVouchers(PurgeVouchersRequest request)
	{
		return new PurgeVouchers(this).execute(new PurgeVouchersResponse(), request, context.getOperatorID());
	}

	@Override
	public GetPurgeVouchersTaskInfoResponse getPurgeVouchersTaskInfo(GetPurgeVouchersTaskInfoRequest request)
	{
		return new GetPurgeVouchersTaskInfo(this).execute(new GetPurgeVouchersTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public GenerateVoucherDetailsReportResponse generateVoucherDetailsReport(GenerateVoucherDetailsReportRequest request)
	{
		return new GenerateVoucherDetailsReport(this).execute(new GenerateVoucherDetailsReportResponse(), request, context.getOperatorID());
	}

	@Override
	public GetGenerateVoucherDetailsReportTaskInfoResponse getGenerateVoucherDetailsReportTaskInfo(GetGenerateVoucherDetailsReportTaskInfoRequest request)
	{
		return new GetGenerateVoucherDetailsReportTaskInfo(this).execute(new GetGenerateVoucherDetailsReportTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public GenerateVoucherDistributionReportResponse generateVoucherDistributionReport(GenerateVoucherDistributionReportRequest request)
	{
		return new GenerateVoucherDistributionReport(this).execute(new GenerateVoucherDistributionReportResponse(), request, context.getOperatorID());
	}

	@Override
	public GetGenerateVoucherDistributionReportTaskInfoResponse getGenerateVoucherDistributionReportTaskInfo(GetGenerateVoucherDistributionReportTaskInfoRequest request)
	{
		return new GetGenerateVoucherDistributionReportTaskInfo(this).execute(new GetGenerateVoucherDistributionReportTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public GenerateVoucherUsageReportResponse generateVoucherUsageReport(GenerateVoucherUsageReportRequest request)
	{
		return new GenerateVoucherUsageReport(this).execute(new GenerateVoucherUsageReportResponse(), request, context.getOperatorID());
	}

	@Override
	public GetGenerateVoucherUsageReportTaskInfoResponse getGenerateVoucherUsageReportTaskInfo(GetGenerateVoucherUsageReportTaskInfoRequest request)
	{
		return new GetGenerateVoucherUsageReportTaskInfo(this).execute(new GetGenerateVoucherUsageReportTaskInfoResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteGenerateVoucherTaskResponse deleteGenerateVoucherTask(DeleteGenerateVoucherTaskRequest request)
	{
		return new DeleteGenerateVoucherTask(this).execute(new DeleteGenerateVoucherTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteLoadVoucherBatchTaskResponse deleteLoadVoucherBatchTask(DeleteLoadVoucherBatchTaskRequest request)
	{
		return new DeleteLoadVoucherBatchTask(this).execute(new DeleteLoadVoucherBatchTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteChangeVoucherStateTaskResponse deleteChangeVoucherStateTask(DeleteChangeVoucherStateTaskRequest request)
	{
		return new DeleteChangeVoucherStateTask(this).execute(new DeleteChangeVoucherStateTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeletePurgeVoucherTaskResponse deletePurgeVoucherTask(DeletePurgeVoucherTaskRequest request)
	{
		return new DeletePurgeVoucherTask(this).execute(new DeletePurgeVoucherTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteVoucherDetailsReportTaskResponse deleteVoucherDetailsReportTask(DeleteVoucherDetailsReportTaskRequest request)
	{
		return new DeleteVoucherDetailsReportTask(this).execute(new DeleteVoucherDetailsReportTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteVoucherDistributionReportTaskResponse deleteVoucherDistributionReportTask(DeleteVoucherDistributionReportTaskRequest request)
	{
		return new DeleteVoucherDistributionReportTask(this).execute(new DeleteVoucherDistributionReportTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public DeleteVoucherUsageReportTaskResponse deleteVoucherUsageReportTask(DeleteVoucherUsageReportTaskRequest request)
	{
		return new DeleteVoucherUsageReportTask(this).execute(new DeleteVoucherUsageReportTaskResponse(), request, context.getOperatorID());
	}

	@Override
	public ReserveVoucherResponse reserveVoucher(ReserveVoucherRequest request)
	{
		return new ReserveVoucher(this).execute(new ReserveVoucherResponse(), request, context.getOperatorID());
	}

	@Override
	public EndReservationResponse endReservation(EndReservationRequest request)
	{
		return new EndReservation(this).execute(new EndReservationResponse(), request, context.getOperatorID());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IVoucherInfo
	//
	// /////////////////////////////////

	@Override
	public boolean getIsMultiOperator()
	{
		return context.getIsMultiOperator();
	}

	@Override
	public String getOperatorID()
	{
		return context.getOperatorID();
	}

	@Override
	public <TReq extends IVsipRequest> int scheduleTask(ScheduledTask<TReq> task, Date executionTime, Recurrence recurrence, Integer recurrenceValue)
	{
		// Set now if executionTime is not specified
		if (executionTime == null)
			executionTime = new Date();
		task.setExecutionTime(executionTime);

		// Calculate the initialDelay
		long initialDelay = executionTime.getTime() - new Date().getTime();
		if (initialDelay < 0)
			initialDelay = 0;

		// Calculate Recurrence
		long period = 0;
		if (recurrence != null || recurrenceValue != null)
		{
			if (recurrence == null || recurrenceValue == null)
				return -1;

			switch (recurrence)
			{
				case daily:
					period = recurrenceValue * MS_PER_DAY;
					break;

				case weekly:
					period = recurrenceValue * 7 * MS_PER_DAY;
					break;

				case monthly:
					period = recurrenceValue * 30 * MS_PER_DAY;
					break;

				default:
					return -1;
			}
		}

		// Assign a Task Number and put it into a Map
		int taskId = nextTaskNumber.getAndIncrement();
		task.setTaskId(taskId);
		taskMap.put(taskId, task);

		try
		{
			if (period == 0)
				task.setFuture(scheduledThreadPool.schedule(task, initialDelay, TimeUnit.MILLISECONDS));
			else
				task.setFuture(scheduledThreadPool.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS));

		}
		catch (RejectedExecutionException ex)
		{
			return -1;
		}

		return taskId;
	}

	@Override
	public <TReq extends IVsipRequest> List<ScheduledTask<TReq>> getTaskInfo(Integer taskId, Class<TReq> cls)
	{
		List<ScheduledTask<TReq>> result = new ArrayList<ScheduledTask<TReq>>();

		if (taskId != null)
		{
			ScheduledTask<?> task = taskMap.get(taskId);
			if (task != null && task.isOfType(cls))
				result.add((ScheduledTask<TReq>) task);
			return result;
		}

		for (ScheduledTask<?> task : taskMap.values())
		{
			if (task.isOfType(cls))
				result.add((ScheduledTask<TReq>) task);
		}

		return result;
	}

	@Override
	public synchronized int addVoucher(Voucher voucher)
	{
		// Test if Activation Code already Exists
		String activationCode = voucher.getActivationCode();
		if (activationCode == null || activationCode.length() < 8 || activationMap.containsKey(activationCode))
			return IVoucherInfo.ADD_RESULT_DUPLICATE_ACTIVATION;

		// Test if Serial Number already exists
		String key = voucher.getKey();
		if (serialMap.containsKey(key))
			return IVoucherInfo.ADD_RESULT_DUPLICATE_SERIAL;

		// Add to Maps
		serialMap.put(key, voucher);
		activationMap.put(activationCode, voucher);

		return IVoucherInfo.ADD_RESULT_OK;
	}

	@Override
	public Voucher getVoucher(String serialNumber, String activationCode, String networkOperatorId)
	{
		boolean hasSerialNumber = serialNumber != null && serialNumber.length() > 0;
		boolean hasActivationCode = activationCode != null && activationCode.length() > 0;

		// Cannot have both or neither
		if (!(hasSerialNumber ^ hasActivationCode))
			return null;

		// Use serialNumber
		if (hasSerialNumber)
		{
			String key = Voucher.getKey(serialNumber, networkOperatorId);
			return serialMap.get(key);
		}

		// Use activationCode
		if (hasActivationCode)
		{
			Voucher voucher = activationMap.get(activationCode);
			return equalsIgnoreCaseEmpty(voucher.getNetworkOperatorId(), networkOperatorId) ? voucher : null;
		}

		return null;
	}

	private boolean equalsIgnoreCase(String text1, String text2)
	{
		if (text1 == null)
			return text2 == null;
		else
			return text1.equalsIgnoreCase(text2);
	}

	private boolean equalsIgnoreCaseEmpty(String text1, String text2)
	{
		if (text1 == null)
			return text2 == null || text2.isEmpty();
		else
			return text1.equalsIgnoreCase(text2);
	}

	@Override
	public boolean purge(String networkOperatorId, Date expiryDate, String currentState, Boolean purgeVouchers, Boolean outputVAC)
	{
		for (String key : serialMap.keySet())
		{
			Voucher voucher = serialMap.get(key);
			if (voucher == null)
				continue;

			if (voucher.setPendingPurge(networkOperatorId, expiryDate, currentState))
			{
				serialMap.remove(key);
				activationMap.remove(voucher.getActivationCode());
			}
		}

		return true;
	}

	private void expireReservations()
	{
		Date cutoffTime = DateTime.getNow().addSeconds(-CLEANUP_INTERVAL_S);
		for (String key : serialMap.keySet())
		{
			Voucher voucher = serialMap.get(key);
			if (voucher == null)
				continue;

			voucher.expireReservation(cutoffTime);
		}
	}

	@Override
	public <TReq extends IVsipRequest> int deleteTask(int taskId, String networkOperatorId, Class<TReq> cls)
	{
		ScheduledTask<?> task = taskMap.get(taskId);
		if (task == null || !task.isOfType(cls) || !equalsIgnoreCase(networkOperatorId, task.getNetworkOperatorId()))
			return Protocol.RESPONSECODE_DATABASE_ERROR;

		if (!task.getFuture().cancel(false))
			return Protocol.RESPONSECODE_DATABASE_ERROR;

		task.setTaskStatus(Protocol.TASKSTATUS_FAILED);
		task.setFailReason("deleted");
		task.setExecutionTime(new Date());

		return Protocol.RESPONSECODE_SUCCESS;
	}

	@Override
	public int getInjectedResponse(String methodName)
	{
		InjectedResponse injectedResponse = injectedResponses.get(methodName.toLowerCase());
		if (injectedResponse == null)
			return Protocol.RESPONSECODE_SUCCESS;

		return injectedResponse.getResponse();

	}

}
