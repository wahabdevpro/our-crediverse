package hxc.services.vssim;

import static org.junit.Assert.*;

import hxc.configuration.ValidationException;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.vssim.VoucherSimService.VoucherSimConfiguration;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.vsip.ChangeVoucherStateRequest;
import hxc.utils.protocol.vsip.ChangeVoucherStateResponse;
import hxc.utils.protocol.vsip.ChangeVoucherStateSchedulation;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskRequest;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskResponse;
import hxc.utils.protocol.vsip.EndReservationRequest;
import hxc.utils.protocol.vsip.EndReservationResponse;
import hxc.utils.protocol.vsip.GenerateVoucherRequest;
import hxc.utils.protocol.vsip.GenerateVoucherResponse;
import hxc.utils.protocol.vsip.GenerateVoucherSchedulation;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoTasks;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoRequest;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoResponse;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoTasks;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallResponse;
import hxc.utils.protocol.vsip.GetVoucherDetailsRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryTransactionRecords;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;
import hxc.utils.protocol.vsip.PurgeVouchersResponse;
import hxc.utils.protocol.vsip.PurgeVouchersSchedulation;
import hxc.utils.protocol.vsip.ReserveVoucherRequest;
import hxc.utils.protocol.vsip.ReserveVoucherResponse;
import hxc.utils.protocol.vsip.UpdateVoucherStateRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateResponse;
import hxc.utils.protocol.vsip.VsipCalls;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;
	private VoucherSimService voucherService;

	private static final String MVNO_ID = "CS";
	private static final String SERIAL_NUMBER = "A1234567";
	private static final String OPERATOR_ID = "c4u";
	private static final String SUBSCRIBER_ID = "0824452655";
	private static final String TRANSACTION_ID = "0000123";
	private static final String SERIAL_NUMBER_FIRST = "A2234561";
	private static final String SERIAL_NUMBER_LAST = "A2234570";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		logger = new LoggerService();
		esb.registerService(logger);

		esb.registerService(new VoucherSimService());

		NumberPlanService numberPlan = new NumberPlanService();
		esb.registerService(numberPlan);
		esb.registerConnector(new MySqlConnector());

		boolean started = esb.start(null);
		assert (started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tear Down
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{

		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testBasic() throws InterruptedException
	{
		// Happy Path Test
		voucherService = esb.getFirstService(VoucherSimService.class);
		IVoucherSim simulator = voucherService.getVoucherSimulator();

		VoucherSimConfiguration config = (VoucherSimConfiguration) voucherService.getConfiguration();
		config.setIsMultiOperator(true);
		IVoucherSim sim = voucherService.getVoucherSimulator();

		DateTime now = DateTime.getNow();

		// Connect via XML RPC
		{
			GetVoucherDetailsRequest request = new GetVoucherDetailsRequest();
			request.setSerialNumber(SERIAL_NUMBER);
			request.setNetworkOperatorId(MVNO_ID);
			GetVoucherDetailsCallRequest callRequest = new GetVoucherDetailsCallRequest();
			callRequest.setRequest(request);

			String url = String.format("http://localhost:%d%s", config.getVoucherPort(), config.getVoucherPath());
			XmlRpcClient client = new XmlRpcClient(url);

			GetVoucherDetailsCallResponse callResponse = null;
			try (XmlRpcConnection connection = client.getConnection())
			{
				connection.setBasicAuthorization(OPERATOR_ID, "c4u");
				callResponse = connection.call(callRequest, GetVoucherDetailsCallResponse.class);
			}
			catch (Exception e)
			{
				assertTrue(false);
			}

			assertEquals(Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST, callResponse.getResponse().getResponseCode());

		}

		// Generate Voucher
		int taskId = 0;
		{
			GenerateVoucherRequest request = new GenerateVoucherRequest();
			request.setNumberOfVouchers(2);
			request.setActivationCodeLength(8);
			request.setCurrency("ZAR");
			request.setSerialNumber(SERIAL_NUMBER);
			request.setValue(100);
			request.setVoucherGroup("Test");
			request.setExpiryDate(now.addDays(1));
			request.setAgent("AdB");
			request.setExtensionText1("1");
			request.setExtensionText2("2");
			request.setExtensionText3("3");
			GenerateVoucherSchedulation schedulation = new GenerateVoucherSchedulation();
			schedulation.setExecutionTime(DateTime.getNow().addSeconds(5));
			request.setSchedulation(schedulation);
			request.setNetworkOperatorId(MVNO_ID);
			GenerateVoucherResponse response = sim.generateVoucher(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			taskId = response.getTaskId();
		}

		// Get Voucher Details
		{
			GetVoucherDetailsRequest request = new GetVoucherDetailsRequest();
			request.setSerialNumber(SERIAL_NUMBER);
			request.setNetworkOperatorId(MVNO_ID);
			GetVoucherDetailsResponse response = sim.getVoucherDetails(request);
			assertEquals(Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST, response.getResponseCode());
		}

		// Wait for generation to complete and try again
		String activationCode;
		{
			Thread.sleep(6000);
			GetVoucherDetailsRequest request = new GetVoucherDetailsRequest();
			request.setSerialNumber(SERIAL_NUMBER);
			request.setNetworkOperatorId(MVNO_ID);
			GetVoucherDetailsResponse response = sim.getVoucherDetails(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());

			activationCode = response.getActivationCode();
			assertEquals(8, activationCode.length());
			assertEquals("ZAR", response.getCurrency());
			assertEquals(SERIAL_NUMBER, response.getSerialNumber());
			assertEquals(100, response.getValue());
			assertEquals("Test", response.getVoucherGroup());
			assertEquals(now.addDays(1), response.getExpiryDate());
			assertEquals("AdB", response.getAgent());
			assertEquals("1", response.getExtensionText1());
			assertEquals("2", response.getExtensionText2());
			assertEquals("3", response.getExtensionText3());
		}

		// Inject response
		sim.injectSelectiveResponse(VsipCalls.GetVoucherDetails, Protocol.RESPONSECODE_RESERVED, 1, 2);

		for (int pass = 0; pass < 6; pass++)
		{
			// Get Voucher Details
			{
				GetVoucherDetailsRequest request = new GetVoucherDetailsRequest();
				request.setSerialNumber(SERIAL_NUMBER);
				request.setNetworkOperatorId(MVNO_ID);
				GetVoucherDetailsResponse response = sim.getVoucherDetails(request);
				if (pass == 1 || pass == 2)
					assertEquals(Protocol.RESPONSECODE_RESERVED, response.getResponseCode());
				else
					assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			}
		}

		// Test Generation overwrite
		int taskId2;
		{
			GenerateVoucherRequest request = new GenerateVoucherRequest();
			request.setNumberOfVouchers(2);
			request.setActivationCodeLength(8);
			request.setCurrency("ZAR");
			request.setSerialNumber(SERIAL_NUMBER);
			request.setValue(100);
			request.setVoucherGroup("Test");
			request.setExpiryDate(now.addDays(1));
			request.setAgent("AdB");
			request.setExtensionText1("1");
			request.setExtensionText2("2");
			request.setExtensionText3("3");
			GenerateVoucherSchedulation schedulation = new GenerateVoucherSchedulation();
			schedulation.setExecutionTime(DateTime.getNow());
			request.setSchedulation(schedulation);
			request.setNetworkOperatorId(MVNO_ID);
			GenerateVoucherResponse response = sim.generateVoucher(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			taskId2 = response.getTaskId();
		}

		// Get the task Info for the failed Generation
		{
			Thread.sleep(1000);
			GetGenerateVoucherTaskInfoRequest request = new GetGenerateVoucherTaskInfoRequest();
			request.setTaskId(taskId2); // All
			request.setNetworkOperatorId(MVNO_ID);
			GetGenerateVoucherTaskInfoResponse response = sim.getGenerateVoucherTaskInfo(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			GetGenerateVoucherTaskInfoTasks[] tasks = response.getTasks();
			assertNotNull(tasks);
			assertEquals(1, tasks.length);
			GetGenerateVoucherTaskInfoTasks task = tasks[0];
			assertEquals(taskId2, task.getTaskId());
			assertEquals(Protocol.TASKSTATUS_FAILED, task.getTaskStatus());
		}

		// Begin Reservation
		{
			ReserveVoucherRequest request = new ReserveVoucherRequest();
			request.setActivationCode(activationCode);
			request.setOperatorId(OPERATOR_ID);
			request.setSubscriberId(SUBSCRIBER_ID);
			request.setTransactionId(TRANSACTION_ID);
			request.setNetworkOperatorId(MVNO_ID);
			ReserveVoucherResponse response = sim.reserveVoucher(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
		}

		// End Reservation
		{
			EndReservationRequest request = new EndReservationRequest();
			request.setActivationCode(activationCode);
			request.setAction(Protocol.ACTION_COMMIT);
			request.setSubscriberId(SUBSCRIBER_ID);
			request.setTransactionId(TRANSACTION_ID);
			request.setNetworkOperatorId(MVNO_ID);
			EndReservationResponse response = sim.endReservation(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
		}

		// Get Voucher History
		{
			GetVoucherHistoryRequest request = new GetVoucherHistoryRequest();
			request.setSerialNumber(SERIAL_NUMBER);
			request.setNetworkOperatorId(MVNO_ID);
			GetVoucherHistoryResponse response = sim.getVoucherHistory(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());

			assertEquals(8, activationCode.length());
			assertEquals("ZAR", response.getCurrency());
			assertEquals(100, response.getValue());
			assertEquals("Test", response.getVoucherGroup());
			assertEquals(now.addDays(1), response.getExpiryDate());
			assertEquals("AdB", response.getAgent());
			assertEquals("1", response.getExtensionText1());
			assertEquals("2", response.getExtensionText2());
			assertEquals("3", response.getExtensionText3());
			assertEquals("10000", response.getBatchId());
			assertEquals(Protocol.STATE_USED, response.getState());
			GetVoucherHistoryTransactionRecords[] history = response.getTransactionRecords();
			assertEquals(3, history.length);
			assertEquals(Protocol.STATE_AVAILABLE, history[0].getNewState());
			assertEquals(Protocol.STATE_RESERVED, history[1].getNewState());
			assertEquals(Protocol.STATE_USED, history[2].getNewState());
			assertEquals(false, response.getVoucherExpired());
			assertNull(response.getSupplierId());
		}

		// Update Voucher State
		{
			UpdateVoucherStateRequest request = new UpdateVoucherStateRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setSerialNumber(SERIAL_NUMBER);
			request.setOldState(Protocol.STATE_UNAVAILABLE);
			request.setNewState(Protocol.STATE_STOLEN);

			UpdateVoucherStateResponse response = sim.updateVoucherState(request);
			assertNotNull(response);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
		}

		// Purge Vouchers
		{
			PurgeVouchersRequest request = new PurgeVouchersRequest();
			request.setExpiryDate(now.addDays(1));
			request.setOffset(null);
			request.setState(Protocol.STATE_STOLEN);
			request.setNetworkOperatorId(MVNO_ID);
			request.setPurgeVouchers(true);
			request.setOutputVAC(false);

			PurgeVouchersSchedulation schedule = new PurgeVouchersSchedulation();
			schedule.setExecutionTime(DateTime.getNow().addSeconds(1));

			request.setSchedulation(schedule);
			PurgeVouchersResponse response = sim.purgeVouchers(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			taskId = response.getTaskId();
		}

		// Verify the voucher is gone

		{
			Thread.sleep(1000);
			GetVoucherDetailsRequest request = new GetVoucherDetailsRequest();
			request.setSerialNumber(SERIAL_NUMBER);
			request.setNetworkOperatorId(MVNO_ID);
			GetVoucherDetailsResponse response = sim.getVoucherDetails(request);
			assertEquals(Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST, response.getResponseCode());
		}

		// Get Purge Voucher Task Info
		{
			GetPurgeVouchersTaskInfoRequest request = new GetPurgeVouchersTaskInfoRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setTaskId(taskId);
			GetPurgeVouchersTaskInfoResponse response = sim.getPurgeVouchersTaskInfo(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			GetPurgeVouchersTaskInfoTasks[] tasks = response.getTasks();
			assertNotNull(tasks);
			assertEquals(1, tasks.length);
			GetPurgeVouchersTaskInfoTasks task = tasks[0];
			assertEquals(taskId, task.getTaskId());
			assertEquals(Protocol.TASKSTATUS_COMPLETED, task.getTaskStatus());
			assertEquals(OPERATOR_ID, task.getOperatorId());
			GetPurgeVouchersTaskInfoTaskData info = task.getTaskData();
			assertNotNull(info);
			assertEquals(Protocol.STATE_STOLEN, info.getState());
		}

		// Purge Vouchers (again)
		{
			PurgeVouchersRequest request = new PurgeVouchersRequest();
			request.setExpiryDate(now.addDays(1));
			request.setOffset(null);
			request.setState(Protocol.STATE_USED);
			request.setNetworkOperatorId(MVNO_ID);
			request.setPurgeVouchers(true);
			request.setOutputVAC(false);
			PurgeVouchersSchedulation schedulation = new PurgeVouchersSchedulation();
			schedulation.setExecutionTime(now.addHours(1));
			request.setSchedulation(schedulation);
			PurgeVouchersResponse response = sim.purgeVouchers(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			taskId = response.getTaskId();
		}

		// Delete the purge voucher task
		{
			DeletePurgeVoucherTaskRequest request = new DeletePurgeVoucherTaskRequest();
			request.setTaskId(taskId);
			request.setNetworkOperatorId(MVNO_ID);
			DeletePurgeVoucherTaskResponse response = sim.deletePurgeVoucherTask(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
		}

		// Get Purge Voucher Task Info (again)
		{
			GetPurgeVouchersTaskInfoRequest request = new GetPurgeVouchersTaskInfoRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setTaskId(taskId);
			GetPurgeVouchersTaskInfoResponse response = sim.getPurgeVouchersTaskInfo(request);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			GetPurgeVouchersTaskInfoTasks[] tasks = response.getTasks();
			assertNotNull(tasks);
			assertEquals(1, tasks.length);
			GetPurgeVouchersTaskInfoTasks task = tasks[0];
			assertEquals(taskId, task.getTaskId());
			assertEquals(Protocol.TASKSTATUS_FAILED, task.getTaskStatus());
			assertEquals(OPERATOR_ID, task.getOperatorId());
			assertEquals("deleted", task.getFailReason());
			GetPurgeVouchersTaskInfoTaskData info = task.getTaskData();
			assertNotNull(info);
			assertEquals(Protocol.STATE_USED, info.getState());
		}

		// Change Voucher State
		{
			GenerateVoucherRequest generateVoucherRequest = new GenerateVoucherRequest();
			generateVoucherRequest.setNumberOfVouchers(10);
			generateVoucherRequest.setActivationCodeLength(8);
			generateVoucherRequest.setCurrency("ZAR");
			generateVoucherRequest.setSerialNumber(SERIAL_NUMBER_FIRST);
			generateVoucherRequest.setValue(100);
			generateVoucherRequest.setVoucherGroup("Test");
			generateVoucherRequest.setExpiryDate(now.addDays(1));
			generateVoucherRequest.setAgent("AdB");
			generateVoucherRequest.setExtensionText1("1");
			generateVoucherRequest.setExtensionText2("2");
			generateVoucherRequest.setExtensionText3("3");
			generateVoucherRequest.setNetworkOperatorId(MVNO_ID);
			GenerateVoucherResponse generateVoucherResponse = sim.generateVoucher(generateVoucherRequest);
			assertNotNull(generateVoucherResponse);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, generateVoucherResponse.getResponseCode());

			ChangeVoucherStateRequest request = new ChangeVoucherStateRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setSerialNumber(SERIAL_NUMBER_FIRST);
			request.setNewState(Protocol.STATE_DAMAGED);

			ChangeVoucherStateResponse response = sim.changeVoucherState(request);
			assertNotNull(response);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());

			GetVoucherDetailsRequest voucherDetailsRequest = new GetVoucherDetailsRequest();
			voucherDetailsRequest.setNetworkOperatorId(MVNO_ID);
			voucherDetailsRequest.setSerialNumber(SERIAL_NUMBER_FIRST);

			GetVoucherDetailsResponse voucherDetailsResponse = sim.getVoucherDetails(voucherDetailsRequest);
			assertNotNull(voucherDetailsResponse);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, voucherDetailsResponse.getResponseCode());
			assertEquals(Protocol.STATE_DAMAGED, voucherDetailsResponse.getState());

			request = new ChangeVoucherStateRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setSerialNumberFirst(SERIAL_NUMBER_FIRST);
			request.setSerialNumberLast(SERIAL_NUMBER_LAST);
			request.setNewState(Protocol.STATE_UNAVAILABLE);
			ChangeVoucherStateSchedulation changeVoucherStateSchedulation = new ChangeVoucherStateSchedulation();
			changeVoucherStateSchedulation.setExecutionTime(DateTime.getNow().addSeconds(1));
			request.setSchedulation(changeVoucherStateSchedulation);

			response = sim.changeVoucherState(request);
			assertNotNull(response);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());

			Thread.sleep(1200);
		}

		// Get Change Voucher State Task Info
		{
			GetChangeVoucherStateTaskInfoRequest request = new GetChangeVoucherStateTaskInfoRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setTaskId(null);

			GetChangeVoucherStateTaskInfoResponse response = sim.getChangeVoucherStateTaskInfo(request);
			assertNotNull(response);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
			assertNotNull(response.getTasks());
			assertEquals(2, response.getTasks().length);
			assertNotNull(response.getTasks()[1]);
			assertNotNull(response.getTasks()[1].getTaskData());
			assertEquals(SERIAL_NUMBER_FIRST, response.getTasks()[1].getTaskData().getSerialNumberFirst());
			assertEquals(SERIAL_NUMBER_LAST, response.getTasks()[1].getTaskData().getSerialNumberLast());

			GetVoucherDetailsRequest voucherDetailsRequest = new GetVoucherDetailsRequest();
			voucherDetailsRequest.setNetworkOperatorId(MVNO_ID);
			voucherDetailsRequest.setSerialNumber("A2234565");

			GetVoucherDetailsResponse voucherDetailsResponse = sim.getVoucherDetails(voucherDetailsRequest);
			assertNotNull(voucherDetailsResponse);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, voucherDetailsResponse.getResponseCode());
			assertEquals(Protocol.STATE_UNAVAILABLE, voucherDetailsResponse.getState());
		}

		// Update Voucher State
		{
			UpdateVoucherStateRequest request = new UpdateVoucherStateRequest();
			request.setNetworkOperatorId(MVNO_ID);
			request.setSerialNumber(SERIAL_NUMBER_FIRST);
			request.setOldState(Protocol.STATE_UNAVAILABLE);
			request.setNewState(Protocol.STATE_AVAILABLE);

			UpdateVoucherStateResponse response = new UpdateVoucherStateResponse();
			assertNotNull(response);
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponseCode());
		}

		// Reset
		{
			sim.reset();
		}

		// Thread.sleep(12 * 60000L);

	}

}
