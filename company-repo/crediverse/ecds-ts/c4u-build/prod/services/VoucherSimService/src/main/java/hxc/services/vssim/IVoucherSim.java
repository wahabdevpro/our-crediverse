package hxc.services.vssim;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.BindingType;

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
import hxc.utils.protocol.vsip.LoadVoucherBatchFileRequest;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileResponse;
import hxc.utils.protocol.vsip.LoadVoucherCheckRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckResponse;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;
import hxc.utils.protocol.vsip.PurgeVouchersResponse;
import hxc.utils.protocol.vsip.ReserveVoucherRequest;
import hxc.utils.protocol.vsip.ReserveVoucherResponse;
import hxc.utils.protocol.vsip.UpdateVoucherStateRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateResponse;
import hxc.utils.protocol.vsip.VsipCalls;

@WebService
@SOAPBinding(style = Style.RPC, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public interface IVoucherSim
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Housekeeping
	//
	// /////////////////////////////////
	@WebMethod
	public abstract int ping(@WebParam(name = "seq") int seq);

	@WebMethod
	public abstract void reset();

	@WebMethod
	public abstract void injectSelectiveResponse(@WebParam(name = "vsipCall") VsipCalls vsipCall, @WebParam(name = "responseCode") int responseCode, //
			@WebParam(name = "skipCount") int skipCount, @WebParam(name = "failCount") int failCount);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VSIP
	//
	// /////////////////////////////////

	@WebMethod
	public abstract GetVoucherDetailsResponse getVoucherDetails(@WebParam(name = "request") GetVoucherDetailsRequest request);

	@WebMethod
	public abstract GetVoucherHistoryResponse getVoucherHistory(@WebParam(name = "request") GetVoucherHistoryRequest request);

	@WebMethod
	public abstract UpdateVoucherStateResponse updateVoucherState(@WebParam(name = "request") UpdateVoucherStateRequest request);

	@WebMethod
	public abstract LoadVoucherCheckResponse loadVoucherCheck(@WebParam(name = "request") LoadVoucherCheckRequest request);

	@WebMethod
	public abstract GenerateVoucherResponse generateVoucher(@WebParam(name = "request") GenerateVoucherRequest request);

	@WebMethod
	public abstract GetGenerateVoucherTaskInfoResponse getGenerateVoucherTaskInfo(@WebParam(name = "request") GetGenerateVoucherTaskInfoRequest request);

	@WebMethod
	public abstract LoadVoucherBatchFileResponse loadVoucherBatchFile(@WebParam(name = "request") LoadVoucherBatchFileRequest request);

	@WebMethod
	public abstract GetLoadVoucherBatchFileTaskInfoResponse getLoadVoucherBatchFileTaskInfo(@WebParam(name = "request") GetLoadVoucherBatchFileTaskInfoRequest request);

	@WebMethod
	public abstract GetVoucherBatchFilesListResponse getVoucherBatchFilesList(@WebParam(name = "request") GetVoucherBatchFilesListRequest request);

	@WebMethod
	public abstract ChangeVoucherStateResponse changeVoucherState(@WebParam(name = "request") ChangeVoucherStateRequest request);

	@WebMethod
	public abstract GetChangeVoucherStateTaskInfoResponse getChangeVoucherStateTaskInfo(@WebParam(name = "request") GetChangeVoucherStateTaskInfoRequest request);

	@WebMethod
	public abstract PurgeVouchersResponse purgeVouchers(@WebParam(name = "request") PurgeVouchersRequest request);

	@WebMethod
	public abstract GetPurgeVouchersTaskInfoResponse getPurgeVouchersTaskInfo(@WebParam(name = "request") GetPurgeVouchersTaskInfoRequest request);

	@WebMethod
	public abstract GenerateVoucherDetailsReportResponse generateVoucherDetailsReport(@WebParam(name = "request") GenerateVoucherDetailsReportRequest request);

	@WebMethod
	public abstract GetGenerateVoucherDetailsReportTaskInfoResponse getGenerateVoucherDetailsReportTaskInfo(@WebParam(name = "request") GetGenerateVoucherDetailsReportTaskInfoRequest request);

	@WebMethod
	public abstract GenerateVoucherDistributionReportResponse generateVoucherDistributionReport(@WebParam(name = "request") GenerateVoucherDistributionReportRequest request);

	@WebMethod
	public abstract GetGenerateVoucherDistributionReportTaskInfoResponse getGenerateVoucherDistributionReportTaskInfo(@WebParam(name = "request") GetGenerateVoucherDistributionReportTaskInfoRequest request);

	@WebMethod
	public abstract GenerateVoucherUsageReportResponse generateVoucherUsageReport(@WebParam(name = "request") GenerateVoucherUsageReportRequest request);

	@WebMethod
	public abstract GetGenerateVoucherUsageReportTaskInfoResponse getGenerateVoucherUsageReportTaskInfo(@WebParam(name = "request") GetGenerateVoucherUsageReportTaskInfoRequest request);

	@WebMethod
	public abstract DeleteGenerateVoucherTaskResponse deleteGenerateVoucherTask(@WebParam(name = "request") DeleteGenerateVoucherTaskRequest request);

	@WebMethod
	public abstract DeleteLoadVoucherBatchTaskResponse deleteLoadVoucherBatchTask(@WebParam(name = "request") DeleteLoadVoucherBatchTaskRequest request);

	@WebMethod
	public abstract DeleteChangeVoucherStateTaskResponse deleteChangeVoucherStateTask(@WebParam(name = "request") DeleteChangeVoucherStateTaskRequest request);

	@WebMethod
	public abstract DeletePurgeVoucherTaskResponse deletePurgeVoucherTask(@WebParam(name = "request") DeletePurgeVoucherTaskRequest request);

	@WebMethod
	public abstract DeleteVoucherDetailsReportTaskResponse deleteVoucherDetailsReportTask(@WebParam(name = "request") DeleteVoucherDetailsReportTaskRequest request);

	@WebMethod
	public abstract DeleteVoucherDistributionReportTaskResponse deleteVoucherDistributionReportTask(@WebParam(name = "request") DeleteVoucherDistributionReportTaskRequest request);

	@WebMethod
	public abstract DeleteVoucherUsageReportTaskResponse deleteVoucherUsageReportTask(@WebParam(name = "request") DeleteVoucherUsageReportTaskRequest request);

	@WebMethod
	public abstract ReserveVoucherResponse reserveVoucher(@WebParam(name = "request") ReserveVoucherRequest request);

	@WebMethod
	public abstract EndReservationResponse endReservation(@WebParam(name = "request") EndReservationRequest request);

}
