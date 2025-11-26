package hxc.services.vssim;

import static org.junit.Assert.*;
import hxc.services.logging.ILogger;
import hxc.services.logging.LoggerService;
import hxc.utils.protocol.vsip.EndReservationCallRequest;
import hxc.utils.protocol.vsip.EndReservationCallResponse;
import hxc.utils.protocol.vsip.GenerateVoucherCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherCallResponse;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryCallRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryCallResponse;
import hxc.utils.protocol.vsip.IValidationContext;
import hxc.utils.protocol.vsip.LoadVoucherCheckCallRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckCallResponse;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.Recurrence;
import hxc.utils.protocol.vsip.ReserveVoucherCallRequest;
import hxc.utils.protocol.vsip.ReserveVoucherCallResponse;
import hxc.utils.protocol.vsip.UpdateVoucherStateCallRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateCallResponse;
import hxc.utils.xmlrpc.XmlRpcSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VsipProtocolTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testVsipProtocol() throws Exception
	{

		final LoggerService logger = new LoggerService();
		logger.start(null);

		IValidationContext context = new IValidationContext()
		{
			@Override
			public boolean getIsMultiOperator()
			{
				return true;
			}

			@Override
			public ILogger getLogger()
			{
				return logger;
			}

			@Override
			public String getOperatorID()
			{
				return "c4u";
			}

		};

		// Validate each field
		assertTrue(Protocol.validateAction(context, true, "commit"));
		assertFalse(Protocol.validateAction(context, true, "123456"));
		assertTrue(Protocol.validateActivationCode(context, true, "0123456789"));
		assertFalse(Protocol.validateActivationCode(context, true, "1234abcds"));
		assertFalse(Protocol.validateActivationCode(context, true, "1234"));
		assertTrue(Protocol.validateActivationCode(context, true, "01234567890123456789"));
		assertFalse(Protocol.validateActivationCode(context, true, "012345678901234567890"));
		assertTrue(Protocol.validateActivationCodeLength(context, true, 10));
		assertFalse(Protocol.validateActivationCodeLength(context, true, 7));
		assertFalse(Protocol.validateActivationCodeLength(context, true, 21));
		assertTrue(Protocol.validateAdditionalAction(context, true, "commit"));
		assertFalse(Protocol.validateAdditionalAction(context, true, "12354"));
		assertTrue(Protocol.validateAdditionalInfo(context, true, "Example"));
		assertFalse(Protocol.validateAdditionalInfo(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateAgent(context, true, "A1234ds"));
		assertFalse(Protocol.validateAgent(context, true, "!@#$%^"));
		assertFalse(Protocol.validateAgent(context, true, "A12345678"));
		assertTrue(Protocol.validateBatchId(context, true, "BatchId1"));
		assertFalse(Protocol.validateBatchId(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateCurrency(context, true, "EUR"));
		assertFalse(Protocol.validateCurrency(context, true, "EURO"));
		assertTrue(Protocol.validateExecutionTime(context, true, new Date()));
		assertTrue(Protocol.validateExpiryDate(context, true, new Date()));
		assertTrue(Protocol.validateExtensionText1(context, true, "123safdafer34124_=? "));
		assertFalse(Protocol.validateExtensionText1(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateFailReason(context, true, "ExampleFault"));
		assertFalse(Protocol.validateFailReason(context, true, GenerateVoucherRequestXml));
		for (int i = 1000; i < 1008; i++)
			assertTrue(Protocol.validateFaultCode(context, true, i));
		assertFalse(Protocol.validateFaultCode(context, true, 1008));
		assertTrue(Protocol.validateFaultString(context, true, "FaultString"));
		assertFalse(Protocol.validateFaultString(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateFilename(context, true, "File_12.xml"));
		assertFalse(Protocol.validateFilename(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateFromTime(context, true, new Date()));
		assertTrue(Protocol.validateNewState(context, true, "unavailable"));
		assertTrue(Protocol.validateNewState(context, true, "available"));
		assertTrue(Protocol.validateNewState(context, true, "pending"));
		assertTrue(Protocol.validateNewState(context, true, "used"));
		assertTrue(Protocol.validateNewState(context, true, "damaged"));
		assertTrue(Protocol.validateNewState(context, true, "stolen"));
		assertFalse(Protocol.validateNewState(context, true, "doesnotexist"));
		assertTrue(Protocol.validateInitialVoucherState(context, true, "available"));
		assertTrue(Protocol.validateInitialVoucherState(context, true, "unavailable"));
		assertFalse(Protocol.validateInitialVoucherState(context, true, "anything"));
		assertTrue(Protocol.validateNetworkOperatorId(context, "Test1234"));
		assertFalse(Protocol.validateNetworkOperatorId(context, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateNumberOfVouchers(context, true, 100));
		assertFalse(Protocol.validateNumberOfVouchers(context, true, 1000001));
		assertTrue(Protocol.validateOffset(context, true, 500));
		assertFalse(Protocol.validateOffset(context, true, -1));
		assertFalse(Protocol.validateOffset(context, true, 1000));
		assertTrue(Protocol.validateOldState(context, true, "unavailable"));
		assertTrue(Protocol.validateOldState(context, true, "available"));
		assertTrue(Protocol.validateOldState(context, true, "pending"));
		assertTrue(Protocol.validateOldState(context, true, "used"));
		assertTrue(Protocol.validateOldState(context, true, "damaged"));
		assertTrue(Protocol.validateOldState(context, true, "stolen"));
		assertFalse(Protocol.validateOldState(context, true, "doesnotexist"));
		assertTrue(Protocol.validateOperatorId(context, true, "operator–123321"));
		assertFalse(Protocol.validateOperatorId(context, true, GenerateVoucherRequestXml));
		assertTrue(Protocol.validateOutputVAC(context, true, true));
		assertTrue(Protocol.validatePurgeVouchers(context, true, true));
		assertTrue(Protocol.validateRecurrence(context, true, Recurrence.daily));
		assertTrue(Protocol.validateRecurrence(context, true, Recurrence.weekly));
		assertTrue(Protocol.validateRecurrence(context, true, Recurrence.monthly));
		assertTrue(Protocol.validateRecurrenceValue(context, true, 1));
		assertTrue(Protocol.validateRecurrenceValue(context, true, 99999));
		assertFalse(Protocol.validateRecurrenceValue(context, true, 0));
		assertFalse(Protocol.validateRecurrenceValue(context, true, 100000));
		assertTrue(Protocol.validateReportFormat(context, true, 0));
		assertTrue(Protocol.validateReportFormat(context, true, 1));
		assertFalse(Protocol.validateReportFormat(context, true, 2));
		assertEquals(0, Protocol.RESPONSECODE_SUCCESS);
		assertEquals(10, Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST);
		assertEquals(11, Protocol.RESPONSECODE_VOUCHER_ALREADY_USED);
		assertEquals(12, Protocol.RESPONSECODE_VOUCHER_MISSING_STOLEN);
		assertEquals(13, Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE);
		assertEquals(100, Protocol.RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER);
		assertEquals(101, Protocol.RESPONSECODE_VOUCHER_RESERVED_SAME_SUBSCRIBER);
		assertEquals(102, Protocol.RESPONSECODE_VOUCHER_EXPIRED);
		assertEquals(103, Protocol.RESPONSECODE_RESERVED);
		assertEquals(104, Protocol.RESPONSECODE_SUBSCRIBER_ID_MISMATCH);
		assertEquals(105, Protocol.RESPONSECODE_VOUCHER_NOT_RESERVED);
		assertEquals(106, Protocol.RESPONSECODE_TRANSACTION_ID_MISMATCH);
		assertEquals(107, Protocol.RESPONSECODE_VOUCHER_DAMAGED);
		assertEquals(108, Protocol.RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER);
		assertEquals(109, Protocol.RESPONSECODE_DATABASE_ERROR);
		assertEquals(110, Protocol.RESPONSECODE_BAD_STATE_TRANSITION);
		assertEquals(111, Protocol.RESPONSECODE_STATE_CHANGE_LIMITS_EXCEEDED);
		assertEquals(200, Protocol.RESPONSECODE_TASK_DOESNT_EXIST);
		assertEquals(201, Protocol.RESPONSECODE_CANNOT_DELETE_RUNNING_TASK);
		// TODO Serial Number Page 75 of Vsip V2
		assertTrue(Protocol.validateState(context, true, "unavailable"));
		assertTrue(Protocol.validateState(context, true, "available"));
		assertTrue(Protocol.validateState(context, true, "pending"));
		assertTrue(Protocol.validateState(context, true, "used"));
		assertTrue(Protocol.validateState(context, true, "damaged"));
		assertFalse(Protocol.validateState(context, true, "any"));
		assertTrue(Protocol.validateSubscriberId(context, true, "012345678901234"));
		assertFalse(Protocol.validateSubscriberId(context, true, "A123567"));
		assertTrue(Protocol.validateTaskId(context, true, 0));
		assertTrue(Protocol.validateTaskId(context, true, 99999999));
		assertFalse(Protocol.validateTaskId(context, true, 100000000));
		assertTrue(Protocol.validateTaskStatus(context, true, "ordered"));
		assertTrue(Protocol.validateTaskStatus(context, true, "completed"));
		assertTrue(Protocol.validateTaskStatus(context, true, "failed"));
		assertTrue(Protocol.validateTaskStatus(context, true, "running"));
		assertFalse(Protocol.validateTaskStatus(context, true, "any"));
		assertTrue(Protocol.validateTimestamp(context, true, new Date()));
		assertTrue(Protocol.validateToTime(context, true, new Date()));
		assertTrue(Protocol.validateTransactionId(context, true, "1234343123123123"));
		assertFalse(Protocol.validateTransactionId(context, true, "a231321f"));
		assertFalse(Protocol.validateTransactionId(context, true, "123456789012345678901234345"));
		assertTrue(Protocol.validateValue(context, true, 123L));
		assertTrue(Protocol.validateVoucherGroup(context, true, "A5"));
		assertFalse(Protocol.validateVoucherGroup(context, true, "A1G432"));
		assertTrue(Protocol.validateVoucherExpired(context, true, true));
		assertTrue(Protocol.validateSupplierId(context, true, "A123B"));
		assertFalse(Protocol.validateSupplierId(context, true, "A2134414214"));

		// Validate the request and response XML

		XmlRpcSerializer serializer = new XmlRpcSerializer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		InputStream stream = null;

		// Get Voucher Details
		{
			stream = new ByteArrayInputStream(GetVoucherDetailsRequestXml.getBytes());
			GetVoucherDetailsCallRequest request = serializer.deSerialize(stream, GetVoucherDetailsCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("012345678", request.getRequest().getSerialNumber());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(GetVoucherDetailsResponseXml.getBytes());
			GetVoucherDetailsCallResponse response = serializer.deSerialize(stream, GetVoucherDetailsCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(0, response.getResponse().getResponseCode());
			assertEquals("ABZ1001", response.getResponse().getAgent());
			assertEquals("ABC1234", response.getResponse().getBatchId());
			assertEquals("SEK", response.getResponse().getCurrency());
			assertEquals("20030717T00:00:00", sdf.format(response.getResponse().getExpiryDate()));
			assertEquals("VoucherType=1,Market=Europe", response.getResponse().getExtensionText1());
			assertEquals("19990817T00:00:00", response.getResponse().getExtensionText2());
			assertEquals("42", response.getResponse().getExtensionText3());
			assertEquals("administrator", response.getResponse().getOperatorId());
			assertEquals("0455395162", response.getResponse().getSubscriberId());
			assertEquals("20021010T14:39:55", sdf.format(response.getResponse().getTimestamp()));
			assertEquals("A1", response.getResponse().getVoucherGroup());
			assertEquals("2", response.getResponse().getState());
			assertEquals(10000, response.getResponse().getValue());
			assertEquals(true, response.getResponse().getVoucherExpired());
			assertEquals("ABCDEFGH", response.getResponse().getSupplierId());

		}

		// Get Voucher History
		{
			stream = new ByteArrayInputStream(GetVoucherHistoryRequestXml.getBytes());
			GetVoucherHistoryCallRequest request = serializer.deSerialize(stream, GetVoucherHistoryCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("012345678", request.getRequest().getSerialNumber());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(GetVoucherHistoryResponseXml.getBytes());
			GetVoucherHistoryCallResponse response = serializer.deSerialize(stream, GetVoucherHistoryCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
			assertEquals("BZ101", response.getResponse().getAgent());
			assertEquals("ABC1234", response.getResponse().getBatchId());
			assertEquals("EUR", response.getResponse().getCurrency());
			assertEquals("20030717T00:00:00", sdf.format(response.getResponse().getExpiryDate()));
			assertEquals("A1", response.getResponse().getVoucherGroup());
			assertEquals("2", response.getResponse().getState());
			assertEquals(10000, response.getResponse().getValue());
			assertEquals("VoucherType=1,Market=Europe", response.getResponse().getExtensionText1());
			assertEquals("19990817T00:00:00", response.getResponse().getExtensionText2());
			assertEquals("42", response.getResponse().getExtensionText3());
			assertNotNull(response.getResponse().getTransactionRecords());
			assertEquals(3, response.getResponse().getTransactionRecords().length);
			assertEquals("administrator", response.getResponse().getTransactionRecords()[0].getOperatorId());
			assertEquals("5", response.getResponse().getTransactionRecords()[0].getNewState());
			assertEquals("20021009T02:39:55", sdf.format(response.getResponse().getTransactionRecords()[0].getTimestamp()));
			assertEquals("administrator", response.getResponse().getTransactionRecords()[1].getOperatorId());
			assertEquals("0", response.getResponse().getTransactionRecords()[1].getNewState());
			assertEquals("20021010T01:39:54", sdf.format(response.getResponse().getTransactionRecords()[1].getTimestamp()));
			assertEquals("706691616", response.getResponse().getTransactionRecords()[2].getSubscriberId());
			assertEquals("1", response.getResponse().getTransactionRecords()[2].getNewState());
			assertEquals("20021010T01:39:54", sdf.format(response.getResponse().getTransactionRecords()[2].getTimestamp()));
			assertEquals("123456", response.getResponse().getTransactionRecords()[2].getTransactionId());
			assertEquals(true, response.getResponse().getVoucherExpired());
			assertEquals("ABCDEFGH", response.getResponse().getSupplierId());
		}

		// Update Voucher State
		{
			stream = new ByteArrayInputStream(UpdateVoucherStateRequestXml.getBytes());
			UpdateVoucherStateCallRequest request = serializer.deSerialize(stream, UpdateVoucherStateCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("012345678", request.getRequest().getSerialNumber());
			assertEquals("3", request.getRequest().getNewState());
			assertEquals("0", request.getRequest().getOldState());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(UpdateVoucherStateResponseXml.getBytes());
			UpdateVoucherStateCallResponse response = serializer.deSerialize(stream, UpdateVoucherStateCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
		}

		// Load Voucher Check
		{
			stream = new ByteArrayInputStream(LoadVoucherCheckRequestXml.getBytes());
			LoadVoucherCheckCallRequest request = serializer.deSerialize(stream, LoadVoucherCheckCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("00471627612", request.getRequest().getSerialNumberFirst());
			assertEquals("00471627620", request.getRequest().getSerialNumberLast());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(LoadVoucherCheckResponseXml.getBytes());
			LoadVoucherCheckCallResponse response = serializer.deSerialize(stream, LoadVoucherCheckCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(1, response.getResponse().getNumberOfVouchers());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
		}

		// Generate Voucher
		{
			stream = new ByteArrayInputStream(GenerateVoucherRequestXml.getBytes());
			GenerateVoucherCallRequest request = serializer.deSerialize(stream, GenerateVoucherCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals(10000, request.getRequest().getNumberOfVouchers());
			assertEquals(20, request.getRequest().getActivationCodeLength());
			assertEquals("EUR", request.getRequest().getCurrency());
			assertEquals("012345678", request.getRequest().getSerialNumber());
			assertEquals(10050, request.getRequest().getValue());
			assertEquals("A5", request.getRequest().getVoucherGroup());
			assertEquals("20050810T02:00:00", sdf.format(request.getRequest().getExpiryDate()));
			assertEquals("ABDCD01234", request.getRequest().getAgent());
			assertEquals("VoucherType=1,Market=Europe", request.getRequest().getExtensionText1());
			assertEquals("19990817T00:00:00", request.getRequest().getExtensionText2());
			assertEquals("42", request.getRequest().getExtensionText3());
			assertNotNull(request.getRequest().getSchedulation());
			assertEquals("20050817T04:30:00", sdf.format(request.getRequest().getSchedulation().getExecutionTime()));
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(GenerateVoucherResponseXml.getBytes());
			GenerateVoucherCallResponse response = serializer.deSerialize(stream, GenerateVoucherCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(1004, response.getResponse().getTaskId());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
		}

		// Reserve Voucher
		{
			stream = new ByteArrayInputStream(ReserveVoucherRequestXml.getBytes());
			ReserveVoucherCallRequest request = serializer.deSerialize(stream, ReserveVoucherCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("012345678", request.getRequest().getActivationCode());
			assertEquals("commit", request.getRequest().getAdditionalAction());
			assertEquals("0455395162", request.getRequest().getSubscriberId());
			assertEquals("123456", request.getRequest().getTransactionId());
			assertEquals("administrator", request.getRequest().getOperatorId());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(ReserveVoucherResponseXml.getBytes());
			ReserveVoucherCallResponse response = serializer.deSerialize(stream, ReserveVoucherCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
			assertEquals("AZ101", response.getResponse().getAgent());
			assertEquals("SEK", response.getResponse().getCurrency());
			assertEquals("123456", response.getResponse().getSerialNumber());
			assertEquals("A5", response.getResponse().getVoucherGroup());
			assertEquals(10000, response.getResponse().getValue());
			assertEquals("VoucherType=1,Market=Europe", response.getResponse().getExtensionText1());
			assertEquals("19990817T00:00:00", response.getResponse().getExtensionText2());
			assertEquals("42", response.getResponse().getExtensionText3());
			assertEquals("ABCDEFGH", response.getResponse().getSupplierId());
		}

		// End Reservation
		{
			stream = new ByteArrayInputStream(EndReservationRequestXml.getBytes());
			EndReservationCallRequest request = serializer.deSerialize(stream, EndReservationCallRequest.class);

			assertNotNull(request);
			assertNotNull(request.getRequest());
			assertEquals("012345678", request.getRequest().getActivationCode());
			assertEquals("commit", request.getRequest().getAction());
			assertEquals("0455395162", request.getRequest().getSubscriberId());
			assertEquals("1234567", request.getRequest().getTransactionId());
			assertEquals("vno2", request.getRequest().getNetworkOperatorId());

			stream = new ByteArrayInputStream(EndReservationResponseXml.getBytes());
			EndReservationCallResponse response = serializer.deSerialize(stream, EndReservationCallResponse.class);

			assertNotNull(response);
			assertNotNull(response.getResponse());
			assertEquals(Protocol.RESPONSECODE_SUCCESS, response.getResponse().getResponseCode());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Voucher Details
	//
	// /////////////////////////////////

	private static final String GetVoucherDetailsRequestXml = "<?xml version=\"1.0\"?><methodCall><methodName>GetVoucherDetails</methodName><para"
			+ "ms><param><value><struct><member><name>serialNumber</name><value><string>012345678</string></value></member><member><nam"
			+ "e>networkOperatorId</name><value><string>vno2</string></value></member></struct></value></param></params></methodCall>";

	private static final String GetVoucherDetailsResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member><member><name>agent</name><value><string>ABZ1001</string></value><"
			+ "/member><member><name>batchId</name><value><string>ABC1234</string></value></member><member><name>currency</name><value>"
			+ "<string>SEK</string></value></member><member><name>expiryDate</name><value><dateTime.iso8601>20030717T00:00:00</dateTime"
			+ ".iso8601></value></member><member><name>extensionText1</name><value><string>VoucherType=1,Market=Europe</string></value>"
			+ "</member><member><name>extensionText2</name><value><string>19990817T00:00:00</string></value></member><member><name>exte"
			+ "nsionText3</name><value><string>42</string></value></member><member><name>operatorId</name><value><string>administrator<"
			+ "/string></value></member><member><name>subscriberId</name><value><string>0455395162</string></value></member><member><na"
			+ "me>timestamp</name><value><dateTime.iso8601>20021010T14:39:55</dateTime.iso8601></value></member><member><name>voucherGr"
			+ "oup</name><value><string>A1</string></value></member><member><name>state</name><value><string>2</string></value></member><member"
			+ "><name>value</name><value><string>10000</string></value></member><member><name>voucherExpired</name><value><boolean>1</b"
			+ "oolean></value></member><member><name>supplierId</name><value><string>ABCDEFGH</string></value></member></struct></value" + "></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Voucher History
	//
	// /////////////////////////////////

	private static final String GetVoucherHistoryRequestXml = "<?xml version='1.0'?><methodCall><methodName>GetVoucherHistory</methodName><para"
			+ "ms><param><value><struct><member><name>serialNumber</name><value><string>012345678</string></value></member><member><nam"
			+ "e>networkOperatorId</name><value><string>vno2</string></value></member></struct></value></param></params></methodCall>";

	private static final String GetVoucherHistoryResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member><member><name>agent</name><value><string>BZ101</string></value></m"
			+ "ember><member><name>batchId</name><value><string>ABC1234</string></value></member><member><name>currency</name><value><s"
			+ "tring>EUR</string></value></member><member><name>expiryDate</name><value><dateTime.iso8601>20030717T00:00:00</dateTime.i"
			+ "so8601></value></member><member><name>voucherGroup</name><value><string>A1</string></value></member><member><name>state<"
			+ "/name><value><string>2</string></value></member><member><name>value</name><value><string>10000</string></value></member><member>"
			+ "<name>extensionText1</name><value><string>VoucherType=1,Market=Europe</string></value></member><member><name>extensionTe"
			+ "xt2</name><value><string>19990817T00:00:00</string></value></member><member><name>extensionText3</name><value><string>42"
			+ "</string></value></member><member><name>transactionRecords</name><value><array><data><value><struct><member><name>operat"
			+ "orId</name><value><string>administrator</string></value></member><member><name>newState</name><value><string>5</string></value><"
			+ "/member><member><name>timestamp</name><value><dateTime.iso8601>20021009T02:39:55</dateTime.iso8601></value></member></st"
			+ "ruct></value><value><struct><member><name>operatorId</name><value><string>administrator</string></value></member><member"
			+ "><name>newState</name><value><string>0</string></value></member><member><name>timestamp</name><value><dateTime.iso8601>20021010T"
			+ "01:39:54</dateTime.iso8601></value></member></struct></value><value><struct><member><name>subscriberId</name><value><str"
			+ "ing>706691616</string></value></member><member><name>newState</name><value><string>1</string></value></member><member><name>time"
			+ "stamp</name><value><dateTime.iso8601>20021010T01:39:54</dateTime.iso8601></value></member><member><name>transactionId</n"
			+ "ame><value><string>123456</string></value></member></struct></value></data></array></value></member><member><name>vouche"
			+ "rExpired</name><value><boolean>1</boolean></value></member><member><name>supplierId</name><value><string>ABCDEFGH</strin"
			+ "g></value></member></struct></value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Voucher State
	//
	// /////////////////////////////////

	private static final String UpdateVoucherStateRequestXml = "<?xml version='1.0'?><methodCall><methodName>UpdateVoucherState</methodName><par"
			+ "ams><param><value><struct><member><name>serialNumber</name><value><string>012345678</string></value></member><member><na"
			+ "me>newState</name><value><string>3</string></value></member><member><name>oldState</name><value><string>0</string></value></member><memb"
			+ "er><name>networkOperatorId</name><value><string>vno2</string></value></member></struct></value></param></params></method" + "Call>";

	private static final String UpdateVoucherStateResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member></struct></value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Load Voucher Check
	//
	// /////////////////////////////////

	private static final String LoadVoucherCheckRequestXml = "<?xml version='1.0'?><methodCall><methodName>LoadVoucherCheck</methodName><param"
			+ "s><param><value><struct><member><name>serialNumberFirst</name><value><string>00471627612</string></value></member><membe"
			+ "r><name>serialNumberLast</name><value><string>00471627620</string></value></member><member><name>networkOperatorId</name"
			+ "><value><string>vno2</string></value></member></struct></value></param></params></methodCall>";

	private static final String LoadVoucherCheckResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">numberOfVouchers</name><value><i4>1</i4></value></member><member><name>responseCode</name><value><i4>0</i4></value></me" + "mber></struct></value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Generate Voucher (PC)
	//
	// /////////////////////////////////

	private static final String GenerateVoucherRequestXml = "<?xml version='1.0'?><methodCall><methodName>GenerateVoucher</methodName><params"
			+ "><param><value><struct><member><name>numberOfVouchers</name><value><i4>10000</i4></value></member><member><name>activati"
			+ "onCodeLength</name><value><i4>20</i4></value></member><member><name>currency</name><value><string>EUR</string></value></"
			+ "member><member><name>serialNumber</name><value><string>012345678</string></value></member><member><name>value</name><val"
			+ "ue><string>10050</string></value></member><member><name>voucherGroup</name><value><string>A5</string></value></member><me"
			+ "mber><name>expiryDate</name><value><dateTime.iso8601>20050810T00:00:00+0000</dateTime.iso8601></value></member><member><"
			+ "name>agent</name><value><string>ABDCD01234</string></value></member><member><name>extensionText1</name><value><string>Vo"
			+ "ucherType=1,Market=Europe</string></value></member><member><name>extensionText2</name><value><string>19990817T00:00:00</"
			+ "string></value></member><member><name>extensionText3</name><value><string>42</string></value></member><member><name>sche"
			+ "dulation</name><value><struct><member><name>executionTime</name><value><dateTime.iso8601>20050817T02:30:00+0000</dateTim"
			+ "e.iso8601></value></member></struct></value></member><member><name>networkOperatorId</name><value><string>vno2</string><"
			+ "/value></member></struct></value></param></params></methodCall>";

	private static final String GenerateVoucherResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member><member><name>taskId</name><value><i4>1004</i4></value></member></struct>" + "</value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Generate Voucher Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetGenerateVoucherTaskInfoRequestXml = "";

	private static final String GetGenerateVoucherTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Load Voucher Batch File (PC)
	//
	// /////////////////////////////////

	private static final String LoadVoucherBatchFileRequestXml = "";

	private static final String LoadVoucherBatchFileResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Load Voucher Batch File Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetLoadVoucherBatchFileTaskInfoRequestXml = "";

	private static final String GetLoadVoucherBatchFileTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Voucher Batch Files List (PC)
	//
	// /////////////////////////////////

	private static final String GetVoucherBatchFilesListRequestXml = "";

	private static final String GetVoucherBatchFilesListResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Change Voucher State (PC)
	//
	// /////////////////////////////////

	private static String ChangeVoucherStateRequestXml = "";

	private static String ChangeVoucherStateResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Change Voucher State Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetChangeVoucherStateTaskInfoRequestXml = "";

	private static final String GetChangeVoucherStateTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Purge Vouchers (PC)
	//
	// /////////////////////////////////

	private static final String PurgeVouchersRequestXml = "";

	private static final String PurgeVouchersResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Purge Vouchers Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetPurgeVouchersTaskInfoRequestXml = "";

	private static final String GetPurgeVouchersTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Generate Voucher Details Report (PC)
	//
	// /////////////////////////////////

	private static final String GenerateVoucherDetailsReportRequestXml = "";

	private static final String GenerateVoucherDetailsReportResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Generate Voucher Details Report Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetGenerateVoucherDetailsReportTaskInfoRequestXml = "";

	private static final String GetGenerateVoucherDetailsReportTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Generate Voucher Distribution Report (PC)
	//
	// /////////////////////////////////

	private static final String GenerateVoucherDistributionReportRequestXml = "";

	private static final String GenerateVoucherDistributionReportResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Generate Voucher Distribution Report Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetGenerateVoucherDistributionReportTaskInfoRequestXml = "";

	private static final String GetGenerateVoucherDistributionReportTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Generate Voucher Usage Report (PC)
	//
	// /////////////////////////////////

	private static final String GenerateVoucherUsageReportRequestXml = "";

	private static final String GenerateVoucherUsageReportResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Generate Voucher Usage Report Task Info (PC)
	//
	// /////////////////////////////////

	private static final String GetGenerateVoucherUsageReportTaskInfoRequestXml = "";

	private static final String GetGenerateVoucherUsageReportTaskInfoResponseXml = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Reserve Voucher
	//
	// /////////////////////////////////

	private static final String ReserveVoucherRequestXml = "<?xml version='1.0'?><methodCall><methodName>ReserveVoucher</methodName><params>"
			+ "<param><value><struct><member><name>activationCode</name><value><string>012345678</string></value></member><member><name"
			+ ">additionalAction</name><value><string>commit</string></value></member><member><name>subscriberId</name><value><string>0"
			+ "455395162</string></value></member><member><name>transactionId</name><value><string>123456</string></value></member><mem"
			+ "ber><name>operatorId</name><value><string>administrator</string></value></member><member><name>networkOperatorId</name><"
			+ "value><string>vno2</string></value></member></struct></value></param></params></methodCall>";

	private static final String ReserveVoucherResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member><member><name>agent</name><value><string>AZ101</string></value></m"
			+ "ember><member><name>currency</name><value><string>SEK</string></value></member><member><name>serialNumber</name><value><"
			+ "string>123456</string></value></member><member><name>voucherGroup</name><value><string>A5</string></value></member><memb"
			+ "er><name>value</name><value><string>10000</string></value></member><member><name>extensionText1</name><value><string>Vou"
			+ "cherType=1,Market=Europe</string></value></member><member><name>extensionText2</name><value><string>19990817T00:00:00</s"
			+ "tring></value></member><member><name>extensionText3</name><value><string>42</string></value></member><member><name>suppl"
			+ "ierId</name><value><string>ABCDEFGH</string></value></member></struct></value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// End Reservation
	//
	// /////////////////////////////////

	private static final String EndReservationRequestXml = "<?xml version='1.0'?><methodCall><methodName>EndReservation</methodName><params>"
			+ "<param><value><struct><member><name>activationCode</name><value><string>012345678</string></value></member><member><name"
			+ ">action</name><value><string>commit</string></value></member><member><name>subscriberId</name><value><string>0455395162<"
			+ "/string></value></member><member><name>transactionId</name><value><string>1234567</string></value></member><member><name"
			+ ">networkOperatorId</name><value><string>vno2</string></value></member></struct></value></param></params></methodCall>";

	private static final String EndReservationResponseXml = "<?xml version='1.0'?><methodResponse><params><param><value><struct><member><name"
			+ ">responseCode</name><value><i4>0</i4></value></member></struct></value></param></params></methodResponse>";

}
