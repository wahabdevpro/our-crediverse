package hxc.connectors.hsx.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.utils.protocol.hsx.DeliverSMRequest;
import hxc.utils.protocol.hsx.DeliverSMResponse;
import hxc.utils.protocol.hsx.Number;
import hxc.utils.protocol.hsx.SubmitSMRequest;
import hxc.utils.protocol.hsx.SubmitSMResponse;
import hxc.utils.xmlrpc.XmlRpcException;
import hxc.utils.xmlrpc.XmlRpcSerializer;

public class HsxProtocolTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup and tear down
	//
	// /////////////////////////////////

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Xml Samples
	//
	// /////////////////////////////////
	private final String faultXml = "<?xml version=\"1.0\"?><methodResponse><fault><value><struct><member><name>faultCode</name>"
			+ "<value><int>1005</int></value></member><member><name>faultString</name><value><string>Internal server error in ...</string>"
			+ "</value></member></struct></value></fault></methodResponse>";

	private final String submitSMRequestXml = "<?xml version=\"1.0\"?><methodCall><methodName>SubmitSM</methodName><params><param><value><struct><member>"
			+ "<name>requestHeader</name><value><struct><member><name>originTransactionId</name><value><string>123123123</string>"
			+ "</value></member><member><name>originTimeStamp</name><value><dateTime.iso8601>20110104T03:39:32</dateTime.iso8601>"
			+ "</value></member><member><name>originHostName</name><value><string>hxc302</string></value></member><member>"
			+ "<name>originSystemType</name><value><string>smsgw</string></value></member><member><name>originServiceName</name>"
			+ "<value><string>smsgwapp</string></value></member><member><name>originOperatorId</name><value>"
			+ "<string>a4923705-e5dd-4e85-9b6e-4f9449bd75a9</string></value></member><member><name>version</name><value>"
			+ "<string>1.0</string></value></member></struct></value></member><member><name>requestParameters</name><value><struct>"
			+ "<member><name>destinationNumber</name><value><struct><member><name>addressDigits</name><value><string>35010</string>"
			+ "</value></member><member><name>numberType</name><value><int>0</int></value></member><member><name>numberPlan</name>"
			+ "<value><int>1</int></value></member></struct></value></member><member><name>encodingSelection</name><value><array>"
			+ "<data><value><struct><member><name>language</name><value><string>eng</string></value></member><member><name>alphabet</name>"
			+ "<value><string>latn</string></value></member></struct></value></data></array></value></member><member><name>message</name>"
			+ "<value><string>Sample Message Content</string></value></member></struct></value></member></struct></value></param>" + "</params></methodCall>";

	private final String submitSMResponseXml = "<?xml version=\"1.0\"?><methodResponse><params><param><value><struct><member><name>responseHeader</name><value><struct>"
			+ "<member><name>originTransactionId</name><value><string>123123123</string></value></member>" + "<member><name>responseMessage</name><value><string>OK</string></value></member>"
			+ "<member><name>originOperatorId</name><value><string>a4923705-e5dd-4e85-9b6e-4f9449bd75a9</string></value></member><member>"
			+ "<name>responseCode</name><value><i4>0</i4></value></member></struct></value></member><member><name>responseParameters</name>"
			+ "<value><struct><member><name>messageId</name><value><string>msg68234</string></value></member></struct></value></member>" + "</struct></value></param></params></methodResponse>";

	private final String deliverSMRequestXml = "<?xml version=\"1.0\"?><methodCall><methodName>DeliverSM</methodName><params><param><value><struct><member>"
			+ "<name>requestHeader</name><value><struct><member><name>originTransactionId</name><value><string>123123123</string>"
			+ "</value></member><member><name>originTimeStamp</name><value><dateTime.iso8601>20110104T03:39:32</dateTime.iso8601>"
			+ "</value></member><member><name>originHostName</name><value><string>hxc302</string></value></member><member>"
			+ "<name>originSystemType</name><value><string>smsgw</string></value></member><member><name>originServiceName</name>"
			+ "<value><string>smsgwapp</string></value></member><member><name>originOperatorId</name><value>"
			+ "<string>a4923705-e5dd-4e85-9b6e-4f9449bd75a9</string></value></member><member><name>version</name><value><string>1.0</string>"
			+ "</value></member></struct></value></member><member><name>requestParameters</name><value><struct><member>"
			+ "<name>destinationNumber</name><value><struct><member><name>addressDigits</name><value><string>100</string></value>"
			+ "</member><member><name>numberType</name><value><int>3</int></value></member><member><name>numberPlan</name><value>"
			+ "<int>0</int></value></member></struct></value></member><member><name>sourceNumber</name><value><struct><member>"
			+ "<name>addressDigits</name><value><string>680012345</string></value></member><member><name>numberType</name><value>"
			+ "<int>0</int></value></member><member><name>numberPlan</name><value><int>1</int></value></member></struct></value>"
			+ "</member><member><name>messageId</name><value><string>msg89012</string></value></member><member><name>message</name>"
			+ "<value><string>Sample Message Content</string></value></member></struct></value></member></struct></value></param>" + "</params></methodCall>";

	private final String deliverSMResponseXml = "<?xml version=\"1.0\"?><methodResponse><params><param><value><struct><member><name>responseHeader</name><value>"
			+ "<struct><member><name>originTransactionId</name><value><string>123123123</string></value></member><member>" + "<name>responseMessage</name><value><string>OK</string></value></member>"
			+ "<member><name>originOperatorId</name><value><string>a4923705-e5dd-4e85-9b6e-4f9449bd75a9</string></value></member>"
			+ "<member><name>responseCode</name><value><i4>0</i4></value></member></struct></value></member><member>"
			+ "<name>responseParameters</name><value><struct><member><name>messageId</name><value><string>msg89012</string></value>"
			+ "</member></struct></value></member></struct></value></param></params></methodResponse>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////
	@Test
	public void testHsxProtocol() throws XmlRpcException, ParseException
	{
		// NumberType enumeration
		assertEquals(0, Number.NumberType.UNKNOWN.ordinal()); // unknown
		assertEquals(1, Number.NumberType.INTERNATIONAL.ordinal()); // International number
		assertEquals(2, Number.NumberType.NATIONAL.ordinal()); // National number
		assertEquals(3, Number.NumberType.NETWORKSPECIFIC.ordinal()); // Network specific number
		assertEquals(4, Number.NumberType.SUBSCRIBER.ordinal()); // Subscriber number
		assertEquals(5, Number.NumberType.ALPHANUMERIC.ordinal()); // Alphanumeric
		assertEquals(6, Number.NumberType.ABBREVIATED.ordinal()); // Abbreviated number

		// NumberPlan enumeration
		assertEquals(0, Number.NumberPlan.UNKNOWN.ordinal()); // Unknown
		assertEquals(1, Number.NumberPlan.ISDN.ordinal()); // ISDN Numbering plan
		assertEquals(3, Number.NumberPlan.DATA.ordinal()); // Data numbering plan
		assertEquals(4, Number.NumberPlan.TELEX.ordinal()); // Telex numbering plan
		assertEquals(8, Number.NumberPlan.NATIONAL.ordinal()); // National numbering plan
		assertEquals(9, Number.NumberPlan.PRIVATE.ordinal()); // Private numbering plan
		assertEquals(10, Number.NumberPlan.ERMES.ordinal()); // ERMES numbering plan

		// Test SubmitSM Request
		XmlRpcSerializer serializer = new XmlRpcSerializer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		InputStream stream = new ByteArrayInputStream(submitSMRequestXml.getBytes());
		SubmitSMRequest sreq = serializer.deSerialize(stream, SubmitSMRequest.class);
		assertNotNull(sreq);
		assertNotNull(sreq.requestMembers);
		assertNotNull(sreq.requestMembers.requestHeader);
		assertEquals("123123123", sreq.requestMembers.requestHeader.originTransactionId);
		assertEquals(sdf.parse("20110104T03:39:32"), sreq.requestMembers.requestHeader.originTimeStamp);
		assertEquals("hxc302", sreq.requestMembers.requestHeader.originHostName);
		assertEquals("smsgw", sreq.requestMembers.requestHeader.originSystemType);
		assertEquals("smsgwapp", sreq.requestMembers.requestHeader.originServiceName);
		assertEquals("a4923705-e5dd-4e85-9b6e-4f9449bd75a9", sreq.requestMembers.requestHeader.originOperatorId);
		assertEquals("1.0", sreq.requestMembers.requestHeader.version);
		assertNotNull(sreq.requestMembers.requestParameters);
		assertNotNull(sreq.requestMembers.requestParameters.destinationNumber);
		assertEquals("35010", sreq.requestMembers.requestParameters.destinationNumber.addressDigits);
		assertEquals(Number.NumberType.UNKNOWN, sreq.requestMembers.requestParameters.destinationNumber.numberType);
		assertEquals(Number.NumberPlan.ISDN, sreq.requestMembers.requestParameters.destinationNumber.numberPlan);
		assertNotNull(sreq.requestMembers.requestParameters.encodingSelection);
		assertEquals(1, sreq.requestMembers.requestParameters.encodingSelection.length);
		assertEquals("eng", sreq.requestMembers.requestParameters.encodingSelection[0].language);
		assertEquals("latn", sreq.requestMembers.requestParameters.encodingSelection[0].alphabet);
		assertNotNull(sreq.requestMembers.requestParameters.message);
		assertEquals("Sample Message Content", sreq.requestMembers.requestParameters.message);
		assertNull(sreq.requestMembers.requestParameters.sourceNumber);

		// Test SumbitSMResponse
		stream = new ByteArrayInputStream(submitSMResponseXml.getBytes());
		SubmitSMResponse sres = serializer.deSerialize(stream, SubmitSMResponse.class);
		assertNotNull(sres);
		assertNotNull(sres.responseMembers);
		assertNotNull(sres.responseMembers.responseHeader);
		assertEquals("123123123", sres.responseMembers.responseHeader.originTransactionId);
		assertEquals("a4923705-e5dd-4e85-9b6e-4f9449bd75a9", sres.responseMembers.responseHeader.originOperatorId);
		assertEquals(0, sres.responseMembers.responseHeader.responseCode);
		assertEquals("OK", sres.responseMembers.responseHeader.responseMessage);
		assertNotNull(sres.responseMembers.responseParameters);
		assertEquals("msg68234", sres.responseMembers.responseParameters.messageId);

		// Test DeliverSMRequest
		stream = new ByteArrayInputStream(deliverSMRequestXml.getBytes());
		DeliverSMRequest dreq = serializer.deSerialize(stream, DeliverSMRequest.class);
		assertNotNull(dreq);
		assertNotNull(dreq.requestMembers);
		assertNotNull(dreq.requestMembers.requestHeader);
		assertEquals("123123123", dreq.requestMembers.requestHeader.originTransactionId);
		assertEquals(sdf.parse("20110104T03:39:32"), dreq.requestMembers.requestHeader.originTimeStamp);
		assertEquals("hxc302", dreq.requestMembers.requestHeader.originHostName);
		assertEquals("smsgw", dreq.requestMembers.requestHeader.originSystemType);
		assertEquals("smsgwapp", dreq.requestMembers.requestHeader.originServiceName);
		assertEquals("a4923705-e5dd-4e85-9b6e-4f9449bd75a9", dreq.requestMembers.requestHeader.originOperatorId);
		assertEquals("1.0", dreq.requestMembers.requestHeader.version);
		assertNotNull(dreq.requestMembers.requestParameters);
		assertNotNull(dreq.requestMembers.requestParameters.sourceNumber);
		assertEquals("680012345", dreq.requestMembers.requestParameters.sourceNumber.addressDigits);
		assertEquals(Number.NumberPlan.ISDN, dreq.requestMembers.requestParameters.sourceNumber.numberPlan);
		assertEquals(Number.NumberType.UNKNOWN, dreq.requestMembers.requestParameters.sourceNumber.numberType);
		assertNotNull(dreq.requestMembers.requestParameters.destinationNumber);
		assertEquals("100", dreq.requestMembers.requestParameters.destinationNumber.addressDigits);
		assertEquals(Number.NumberPlan.UNKNOWN, dreq.requestMembers.requestParameters.destinationNumber.numberPlan);
		assertEquals(Number.NumberType.NETWORKSPECIFIC, dreq.requestMembers.requestParameters.destinationNumber.numberType);
		assertEquals("Sample Message Content", dreq.requestMembers.requestParameters.message);
		assertEquals("msg89012", dreq.requestMembers.requestParameters.messageId);

		// Test DeliverSMResponse
		stream = new ByteArrayInputStream(deliverSMResponseXml.getBytes());
		DeliverSMResponse dres = serializer.deSerialize(stream, DeliverSMResponse.class);
		assertNotNull(dres);
		assertNotNull(dres.responseMembers);
		assertNotNull(dres.responseMembers.responseHeader);
		assertEquals("123123123", dres.responseMembers.responseHeader.originTransactionId);
		assertEquals("a4923705-e5dd-4e85-9b6e-4f9449bd75a9", dres.responseMembers.responseHeader.originOperatorId);
		assertEquals(0, dres.responseMembers.responseHeader.responseCode);
		assertEquals("OK", dres.responseMembers.responseHeader.responseMessage);
		assertNotNull(dres.responseMembers.responseParameters);
		assertEquals("msg89012", dres.responseMembers.responseParameters.messageId);

		// Test Fault
		boolean caught = false;
		try
		{
			stream = new ByteArrayInputStream(faultXml.getBytes());
			serializer.deSerialize(stream, Object.class);
		}
		catch (XmlRpcException ex)
		{
			// ?? assertEquals("Internal server error in ...", ex.getMessage());
			// ?? assertEquals(1005, ex.getFaultCode());
			caught = true;
		}
		assertTrue(caught);

	}
}
