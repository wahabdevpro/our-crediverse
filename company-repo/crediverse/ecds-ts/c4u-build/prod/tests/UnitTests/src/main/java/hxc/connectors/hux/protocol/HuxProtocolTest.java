package hxc.connectors.hux.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers.Actions;
import hxc.utils.xmlrpc.XmlRpcException;
import hxc.utils.xmlrpc.XmlRpcSerializer;

public class HuxProtocolTest
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
	// XmlExamples
	//
	// /////////////////////////////////

	private final String handleUSSDRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>handleUSSDRequest</methodName>"
			+ "<params><param><value><struct><member><name>TransactionId</name><value><string>00001</string>"
			+ "</value></member><member><name>TransactionTime</name><value><dateTime.iso8601>20060723T14:08:55</dateTime.iso8601>"
			+ "</value></member><member><name>MSISDN</name><value><string>275551234</string></value></member>"
			+ "<member><name>USSDServiceCode</name><value><string>543</string></value></member><member><name>USSDRequestString</name>"
			+ "<value><string>14321*1000#</string></value></member><member><name>response</name><value><string>false</string>" + "</value></member></struct></value></param></params></methodCall>";

	private final String handleUSSDResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member>"
			+ "<name>TransactionId</name><value><string>00001</string></value></member><member><name>TransactionTime</name>"
			+ "<value><dateTime.iso8601>20060723T14:08:56</dateTime.iso8601></value></member><member><name>USSDResponseString</name>"
			+ "<value><string>Your Return Message Here</string></value></member><member><name>encodingSelection</name>"
			+ "<value><array><data><value><struct><member><name>alphabet</name><value><string>LATN</string></value></member><member>"
			+ "<name>language</name><value><string>ENG</string></value></member></struct></value></data></array></value></member>"
			+ "<member><name>action</name><value><string>end</string></value></member></struct></value></param></params></methodResponse>";

    @SuppressWarnings("unused") // this may actually be used in a future test, better to have the reference - even if not currently used
	private final String handleUSSDFaultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><fault><value><struct><member><name>TransactionId</name>"
			+ "<value><string>00001</string></value></member><member><name>TransactionTime</name><value>"
			+ "<dateTime.iso8601>20130120T16:00:56</dateTime.iso8601></value></member><member><name>faultCode</name><value>"
			+ "<int>4001</int></value></member><member><name>faultString</name><value><string>Application busy</string></value>" + "</member></struct></value></fault></methodResponse>";

	private final String handleLatestVersion = "<?xml version=\"1.0\" encoding=\"utf-8\"?><methodCall><methodName>handleUSSDRequest</methodName>"
			+ "<params><param><value><struct><member><name>TransactionId</name><value><string>11537571</string></value></member>"
			+ "<member><name>TransactionTime</name><value><dateTime.iso8601>20161129T11:54:40</dateTime.iso8601></value></member>"
			+ "<member><name>MSISDN</name><value><string>278553610000268</string></value></member><member><name>USSDServiceCode</name>"
			+ "<value><string>910</string></value></member><member><name>USSDRequestString</name><value><string>*2*278553620060864*5*11111#"
			+ "</string></value></member><member><name>response</name><value><string>false</string></value></member><member><name>Sequence</name>"
			+ "<value><i4>1</i4></value></member><member><name>SessionId</name><value><string>11537571</string></value></member><member>"
			+ "<name>cellGlobalId</name><value><struct><member><name>mobileCountryCode</name><value><i4>101</i4></value></member><member>"
			+ "<name>mobileNetworkCode</name><value><i4>10</i4></value></member><member><name>locationAreaCode</name><value><i4>1000</i4></value></member>"
			+ "<member><name>cellIdentity</name><value><i4>1000</i4></value></member></struct></value></member><member><name>vlrNumber</name><value><struct><member>"
			+ "<name>addressDigits</name><value><string>1000</string></value></member><member><name>numberType</name><value><i4>1</i4></value></member><member>"
			+ "<name>numberPlan</name><value><i4>1</i4></value></member></struct></value></member><member><name>mscNumber</name><value><struct><member>"
			+ "<name>addressDigits</name><value><string>1000</string></value></member><member><name>numberType</name><value><i4>1</i4>"
			+ "</value></member><member><name>numberPlan</name><value><i4>1</i4></value></member></struct></value></member><member>"
			+ "<name>IMSI</name><value><string>0123456</string></value></member></struct></value></param></params></methodCall>";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testHuxProtocol() throws XmlRpcException, ParseException, UnsupportedEncodingException, IOException
	{
		XmlRpcSerializer serializer2 = new XmlRpcSerializer();

		try (InputStream stm = new ByteArrayInputStream(handleLatestVersion.getBytes("UTF-8")))
		{
			HandleUSSDRequest xyz = serializer2.deSerialize(stm, HandleUSSDRequest.class);
			assertNotNull(xyz);
		}

		// Actions enumeration
		assertEquals("end", HandleUSSDResponseMembers.Actions.end.toString());
		assertEquals("request", HandleUSSDResponseMembers.Actions.request.toString());
		assertEquals("notify", HandleUSSDResponseMembers.Actions.notify.toString());

		// Test HandleUSSD Request
		XmlRpcSerializer serializer = new XmlRpcSerializer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		InputStream stream = new ByteArrayInputStream(handleUSSDRequestXml.getBytes());
		HandleUSSDRequest hreq = serializer.deSerialize(stream, HandleUSSDRequest.class);
		assertNotNull(hreq);
		assertNotNull(hreq.members);
		assertEquals("00001", hreq.members.TransactionId);
		assertEquals(sdf.parse("20060723T14:08:55"), hreq.members.TransactionTime);
		assertEquals("275551234", hreq.members.MSISDN);
		assertEquals("543", hreq.members.USSDServiceCode);
		assertEquals("14321*1000#", hreq.members.USSDRequestString);
		assertEquals(false, hreq.members.response);
		assertNull(hreq.members.IMSI);
		assertNull(hreq.members.SessionId);
		assertNull(hreq.members.Sequence);
		assertNull(hreq.members.VLR);

		// Test HandleUSSD Request
		stream = new ByteArrayInputStream(handleUSSDResponseXml.getBytes());
		HandleUSSDResponse hres = serializer.deSerialize(stream, HandleUSSDResponse.class);
		assertNotNull(hres);
		assertNotNull(hres.members);
		assertEquals("00001", hres.members.TransactionId);
		assertEquals(sdf.parse("20060723T14:08:56"), hres.members.TransactionTime);
		assertEquals("Your Return Message Here", hres.members.USSDResponseString);
		assertNotNull(hres.members.encodingSelection);
		assertEquals(1, hres.members.encodingSelection.length);
		assertEquals("LATN", hres.members.encodingSelection[0].alphabet);
		assertEquals("ENG", hres.members.encodingSelection[0].language);
		assertEquals(Actions.end, hres.members.action);
		assertNull(hres.members.ResponseCode);

	}
}
