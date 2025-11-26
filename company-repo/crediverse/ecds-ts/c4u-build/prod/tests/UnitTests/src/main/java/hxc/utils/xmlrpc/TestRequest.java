package hxc.utils.xmlrpc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;

import hxc.utils.calendar.DateTime;

@XmlRpcMethod(name = "testRequest")
public class TestRequest
{
	private TestHeader testRequestHeader;
	private TestBody testRequestBody;
	private TestRequestNullParameter testRequestNullParameter;

	public TestHeader getTestRequestHeader()
	{
		return testRequestHeader;
	}

	public void setTestRequestHeader(TestHeader testRequestHeader)
	{
		this.testRequestHeader = testRequestHeader;
	}

	public TestBody getTestRequestBody()
	{
		return testRequestBody;
	}

	public void setTestRequestBody(TestBody testRequestBody)
	{
		this.testRequestBody = testRequestBody;
	}

	public TestRequestNullParameter getTestRequestNullParameter()
	{
		return testRequestNullParameter;
	}

	public void setTestRequestNullParameter(TestRequestNullParameter testRequestNullParameter)
	{
		this.testRequestNullParameter = testRequestNullParameter;
	}

	public static TestRequest Create()
	{
		// Long String
		String longString = "Lorem ipsum dolor sit amet, consectetaur adipisicing elit, sed do eiusmod tempor incididunt ut labore et "
				+ "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea "
				+ "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla "
				+ "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est "
				+ "laborum Et harumd und lookum like Greek to me, dereud facilis est er expedit distinct. Nam liber te conscient "
				+ "to factor tum poen legum odioque civiuda. Et tam neque pecun modut est neque nonor et imper ned libidig met, "
				+ "consectetur adipiscing elit, sed ut labore et dolore magna aliquam makes one wonder who would ever read this stuff? ";

		// Long byte array
		byte[] longByteArray = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

		// Create Request
		TestRequest original = new TestRequest();
		original.setTestRequestHeader(new TestHeader());
		original.setTestRequestBody(new TestBody());
		original.setTestRequestNullParameter(null);

		// Create Header
		original.getTestRequestHeader().setLongString(longString);
		original.getTestRequestHeader().setFunnyString("<john> & <andries>");
		original.getTestRequestHeader().setNullString(null);
		original.getTestRequestHeader().setEmptyString("");

		// Create Body
		original.getTestRequestBody().setNullInt(null);
		original.getTestRequestBody().setNegativeInt(-5);
		original.getTestRequestBody().setPositiveInt(20);
		original.getTestRequestBody().setIgnoreInt(7501);
		original.getTestRequestBody().setNullDouble(null);
		original.getTestRequestBody().setNegativeDouble(-123.45);
		original.getTestRequestBody().setPositiveDouble(789.12);
		original.getTestRequestBody().setNullBoolean(null);
		original.getTestRequestBody().setFalseBoolean(false);
		original.getTestRequestBody().setTrueBoolean(true);
		original.getTestRequestBody().setNullLong(null);
		original.getTestRequestBody().setNegativeLong(-1234567890L);
		original.getTestRequestBody().setPositiveLong(1234567890L);
		original.getTestRequestBody().setNullEnum1(null);
		original.getTestRequestBody().setCharmEnum(TestEnumeration.Charm);
		original.getTestRequestBody().setNullEnum2(null);
		original.getTestRequestBody().setStangeEnum(TestEnumeration.Strange);
		original.getTestRequestBody().setNullByteArray(null);
		original.getTestRequestBody().setEmptyByteArray(new byte[0]);
		original.getTestRequestBody().setShortByteArray(new byte[] { 1 });
		original.getTestRequestBody().setLongByteArray(longByteArray);
		original.getTestRequestBody().setNullDate(null);
		original.getTestRequestBody().setIsoDate(new DateTime(2013, 12, 25, 12, 13, 14));
		original.getTestRequestBody().setDateOnlyFormat(new DateTime(2014, 02, 21, 15, 16, 17));
		original.getTestRequestBody().setNullStructure(null);
		original.getTestRequestBody().setStructure(new TestStructure());
		original.getTestRequestBody().setNullArray(null);
		original.getTestRequestBody().setEmptyArray(new TestStructure[0]);
		original.getTestRequestBody().setArray(new TestStructure[2]);
		original.getTestRequestBody().setIntArray(new int[] { 12, 13, 14 });

		// Create Structure
		original.getTestRequestBody().getStructure().setName("andries");
		original.getTestRequestBody().getStructure().setNumber(23);

		// Create array
		original.getTestRequestBody().getArray()[0] = new TestStructure();
		original.getTestRequestBody().getArray()[0].setName("John");
		original.getTestRequestBody().getArray()[0].setNumber(2);
		original.getTestRequestBody().getArray()[1] = new TestStructure();
		original.getTestRequestBody().getArray()[1].setName("Maartin");
		original.getTestRequestBody().getArray()[1].setNumber(3000);

		return original;

	}

	public void assertSame(TestRequest original)
	{
		TestRequest deserialized = this;

		assertEquals(original.getTestRequestHeader().getLongString(), deserialized.getTestRequestHeader().getLongString());
		assertEquals(original.getTestRequestHeader().getFunnyString(), deserialized.getTestRequestHeader().getFunnyString());
		assertEquals(original.getTestRequestHeader().getNullString(), deserialized.getTestRequestHeader().getNullString());
		assertEquals(original.getTestRequestHeader().getEmptyString(), deserialized.getTestRequestHeader().getEmptyString());

		assertEquals(original.getTestRequestBody().getNullInt(), deserialized.getTestRequestBody().getNullInt());
		assertEquals(original.getTestRequestBody().getNegativeInt(), deserialized.getTestRequestBody().getNegativeInt());
		assertEquals(original.getTestRequestBody().getPositiveInt(), deserialized.getTestRequestBody().getPositiveInt());
		if (deserialized.getTestRequestBody().getIgnoreInt() != 0)
		{
			fail("Ignored value NOT ignored [deserialized.getTestRequestBody().getIgnoreInt()]");
		}

		assertEquals(original.getTestRequestBody().getNullBoolean(), deserialized.getTestRequestBody().getNullBoolean());
		assertEquals(original.getTestRequestBody().getFalseBoolean(), deserialized.getTestRequestBody().getFalseBoolean());
		assertEquals(original.getTestRequestBody().getTrueBoolean(), deserialized.getTestRequestBody().getTrueBoolean());
		assertEquals(original.getTestRequestBody().getNullLong(), deserialized.getTestRequestBody().getNullLong());
		assertEquals(original.getTestRequestBody().getNegativeLong(), deserialized.getTestRequestBody().getNegativeLong());
		assertEquals(original.getTestRequestBody().getPositiveLong(), deserialized.getTestRequestBody().getPositiveLong());
		assertEquals(original.getTestRequestBody().getPositiveLong(), deserialized.getTestRequestBody().getPositiveLong());
		assertEquals(original.getTestRequestBody().getNullEnum1(), deserialized.getTestRequestBody().getNullEnum1());
		assertEquals(original.getTestRequestBody().getCharmEnum(), deserialized.getTestRequestBody().getCharmEnum());
		assertEquals(original.getTestRequestBody().getNullEnum2(), deserialized.getTestRequestBody().getNullEnum2());
		assertEquals(original.getTestRequestBody().getStangeEnum(), deserialized.getTestRequestBody().getStangeEnum());
		assertEquals(original.getTestRequestBody().getNullDate(), deserialized.getTestRequestBody().getNullDate());
		assertEquals(original.getTestRequestBody().getIsoDate(), deserialized.getTestRequestBody().getIsoDate());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try
		{
			if (!sdf.parse(sdf.format(original.getTestRequestBody().getDateOnlyFormat())).equals(deserialized.getTestRequestBody().getDateOnlyFormat()))
			{
				fail("Date pass test failed! [deserialized.getTestRequestBody().getDateOnlyFormat()]");
			}
		}
		catch (Exception e)
		{
			fail("Failed to pass date in test");
		}

		assertNull(deserialized.getTestRequestBody().getNullStructure());
		assertEquals(original.getTestRequestBody().getStructure().getName(), deserialized.getTestRequestBody().getStructure().getName());
		assertEquals(original.getTestRequestBody().getStructure().getNumber(), deserialized.getTestRequestBody().getStructure().getNumber());
		assertNull(deserialized.getTestRequestBody().getNullByteArray());
		// Issue is with XmlRpcClientServerTest.java, line: 53, 54
		// It is referring to the same object when asserting if it is the same and
		// request.getMethodCall() returns a null EmptyByteArray which would result in an error below
		// assertEquals(original.getTestRequestBody().getEmptyByteArray().length, 0);

		assertEquals(original.getTestRequestBody().getShortByteArray().length, deserialized.getTestRequestBody().getShortByteArray().length);
		assertArrayEquals(original.getTestRequestBody().getShortByteArray(), deserialized.getTestRequestBody().getShortByteArray());
		assertEquals(original.getTestRequestBody().getLongByteArray().length, deserialized.getTestRequestBody().getLongByteArray().length);
		assertArrayEquals(original.getTestRequestBody().getLongByteArray(), deserialized.getTestRequestBody().getLongByteArray());
		assertNull(deserialized.getTestRequestBody().getNullArray());
		assertEquals(0, deserialized.getTestRequestBody().getEmptyArray().length);
		assertEquals(original.getTestRequestBody().getIntArray().length, deserialized.getTestRequestBody().getIntArray().length);
		assertArrayEquals(original.getTestRequestBody().getIntArray(), deserialized.getTestRequestBody().getIntArray());
		assertEquals(original.getTestRequestBody().getArray().length, deserialized.getTestRequestBody().getArray().length);
		for (int index = 0; index < original.getTestRequestBody().getArray().length; index++)
		{
			TestStructure expected = original.getTestRequestBody().getArray()[index];
			TestStructure actual = deserialized.getTestRequestBody().getArray()[index];
			assertEquals(expected.getName(), actual.getName());
			assertEquals(expected.getNumber(), actual.getNumber());
		}

	}
}
