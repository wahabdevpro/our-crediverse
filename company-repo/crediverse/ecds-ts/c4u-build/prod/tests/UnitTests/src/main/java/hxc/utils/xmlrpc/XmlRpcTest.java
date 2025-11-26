package hxc.utils.xmlrpc;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class XmlRpcTest
{

	@Test
	public void testXmlRpc()
	{
		// Create Request
		TestRequest original = TestRequest.Create();
		// Serialise
		XmlRpcSerializer serializer = new XmlRpcSerializer();
		String result = serializer.serialize(original);

		// De-Serialize
		InputStream stream = new ByteArrayInputStream(result.getBytes());
		TestRequest deserialized = null;
		boolean passedDeserialise = false;
		try
		{
			deserialized = serializer.deSerialize(stream, TestRequest.class);
			passedDeserialise = true;
		}
		catch (XmlRpcException e)
		{
			fail("DE-Serialisation Failed: " + e.getMessage());
		}

		if (!passedDeserialise)
			return;

		// Test if same
		deserialized.assertSame(original);

	}

	@Test
	public void testXmlRpcSpeed() throws Exception
	{
		final int count = 1000;

		// Create Request
		TestRequest original = TestRequest.Create();

		// Serialise
		XmlRpcSerializer serializer = new XmlRpcSerializer();

		String result = serializer.serialize(original);

		for (int i = 0; i < 10000; i++)
		{
			result = serializer.serialize(original);
		}

		long duration1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			result = serializer.serialize(original);
		}
		duration1 = System.currentTimeMillis() - duration1;

		long duration2 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			result = serializer.serialize(original);
			result = serializer.serialize(original);
		}
		duration2 = System.currentTimeMillis() - duration2 - duration1;
		System.out.println(String.format("serialization = %f\u00B5s", 1000.0 * duration2 / count));

		// Deserialize
		InputStream stream = new ByteArrayInputStream(result.getBytes());
		serializer.deSerialize(stream, TestRequest.class);

		System.gc();
		long duration3 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			stream.reset();
			serializer.deSerialize(stream, TestRequest.class);
		}
		duration3 = System.currentTimeMillis() - duration3;

		System.gc();
		long duration4 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			stream.reset();
			serializer.deSerialize(stream, TestRequest.class);
			stream.reset();
			serializer.deSerialize(stream, TestRequest.class);
		}
		duration4 = System.currentTimeMillis() - duration4 - duration3;
		System.out.println(String.format("de-serialization = %f\u00B5s", 1000.0 * duration4 / count));

	}
}
