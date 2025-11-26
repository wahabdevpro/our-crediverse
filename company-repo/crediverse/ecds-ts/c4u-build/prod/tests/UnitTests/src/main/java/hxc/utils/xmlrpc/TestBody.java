package hxc.utils.xmlrpc;

import java.util.Date;

@XmlRpcMethod(name = "testBody")
public class TestBody extends TestBase
{
	// boolean
	private Boolean nullBoolean;
	private Boolean falseBoolean;
	private boolean trueBoolean;

	public Boolean getNullBoolean()
	{
		return nullBoolean;
	}

	public void setNullBoolean(Boolean nullBoolean)
	{
		this.nullBoolean = nullBoolean;
	}

	public Boolean getFalseBoolean()
	{
		return falseBoolean;
	}

	public void setFalseBoolean(Boolean falseBoolean)
	{
		this.falseBoolean = falseBoolean;
	}

	public boolean getTrueBoolean()
	{
		return trueBoolean;
	}

	public void setTrueBoolean(boolean trueBoolean)
	{
		this.trueBoolean = trueBoolean;
	}

	// other
	@XmlRpcAsString
	private Long nullLong;
	@XmlRpcAsString
	private Long negativeLong;
	@XmlRpcAsString
	private long positiveLong;

	public Long getNullLong()
	{
		return nullLong;
	}

	public void setNullLong(Long nullLong)
	{
		this.nullLong = nullLong;
	}

	public Long getNegativeLong()
	{
		return negativeLong;
	}

	public void setNegativeLong(Long negativeLong)
	{
		this.negativeLong = negativeLong;
	}

	public long getPositiveLong()
	{
		return positiveLong;
	}

	public void setPositiveLong(long positiveLong)
	{
		this.positiveLong = positiveLong;
	}

	// enums
	private TestEnumeration nullEnum1;
	private TestEnumeration charmEnum;
	@XmlRpcAsString
	private TestEnumeration nullEnum2;
	@XmlRpcAsString
	private TestEnumeration stangeEnum;

	public TestEnumeration getNullEnum1()
	{
		return nullEnum1;
	}

	public void setNullEnum1(TestEnumeration nullEnum1)
	{
		this.nullEnum1 = nullEnum1;
	}

	public TestEnumeration getCharmEnum()
	{
		return charmEnum;
	}

	public void setCharmEnum(TestEnumeration charmEnum)
	{
		this.charmEnum = charmEnum;
	}

	public TestEnumeration getNullEnum2()
	{
		return nullEnum2;
	}

	public void setNullEnum2(TestEnumeration nullEnum2)
	{
		this.nullEnum2 = nullEnum2;
	}

	public TestEnumeration getStangeEnum()
	{
		return stangeEnum;
	}

	public void setStangeEnum(TestEnumeration stangeEnum)
	{
		this.stangeEnum = stangeEnum;
	}

	// base64
	private byte[] nullByteArray;
	private byte[] emptyByteArray;
	private byte[] shortByteArray;
	private byte[] longByteArray;

	public byte[] getEmptyByteArray()
	{
		return emptyByteArray;
	}

	public void setEmptyByteArray(byte[] emptyByteArray)
	{
		this.emptyByteArray = emptyByteArray;
	}

	public byte[] getShortByteArray()
	{
		return shortByteArray;
	}

	public void setShortByteArray(byte[] shortByteArray)
	{
		this.shortByteArray = shortByteArray;
	}

	public byte[] getLongByteArray()
	{
		return longByteArray;
	}

	public void setLongByteArray(byte[] longByteArray)
	{
		this.longByteArray = longByteArray;
	}

	public TestStructure[] getNullArray()
	{
		return nullArray;
	}

	public void setNullArray(TestStructure[] nullArray)
	{
		this.nullArray = nullArray;
	}

	// dateTime.iso8601
	private Date nullDate;
	private Date isoDate;
	@XmlRpcFormat(format = "yyyyMMdd")
	private Date dateOnlyFormat;

	public Date getNullDate()
	{
		return nullDate;
	}

	public void setNullDate(Date nullDate)
	{
		this.nullDate = nullDate;
	}

	public Date getIsoDate()
	{
		return isoDate;
	}

	public void setIsoDate(Date isoDate)
	{
		this.isoDate = isoDate;
	}

	public Date getDateOnlyFormat()
	{
		return dateOnlyFormat;
	}

	public void setDateOnlyFormat(Date dateOnlyFormat)
	{
		this.dateOnlyFormat = dateOnlyFormat;
	}

	// struct
	private TestStructure nullStructure;
	private TestStructure structure;

	public TestStructure getNullStructure()
	{
		return nullStructure;
	}

	public void setNullStructure(TestStructure nullStructure)
	{
		this.nullStructure = nullStructure;
	}

	public TestStructure getStructure()
	{
		return structure;
	}

	public void setStructure(TestStructure structure)
	{
		this.structure = structure;
	}

	// Array
	private TestStructure[] nullArray;
	private TestStructure[] emptyArray;
	private TestStructure[] array;
	private int[] intArray;

	public byte[] getNullByteArray()
	{
		return nullByteArray;
	}

	public void setNullByteArray(byte[] nullByteArray)
	{
		this.nullByteArray = nullByteArray;
	}

	public TestStructure[] getEmptyArray()
	{
		return emptyArray;
	}

	public void setEmptyArray(TestStructure[] emptyArray)
	{
		this.emptyArray = emptyArray;
	}

	public TestStructure[] getArray()
	{
		return array;
	}

	public void setArray(TestStructure[] array)
	{
		this.array = array;
	}

	public int[] getIntArray()
	{
		return intArray;
	}

	public void setIntArray(int[] intArray)
	{
		this.intArray = intArray;
	}

}
