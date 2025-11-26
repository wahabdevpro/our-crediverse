package hxc.connectors.database.mysql;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.utils.calendar.DateTime;

@Table(name = "my_test", uniqueIndex = "nonNullShort,nonNullChar")
public class TestClass
{
	public enum Quarks
	{
		UP, DOWN, STRANGE, CHARM, TOP, BOTTOM
	};

	// Primary Key
	@Column(primaryKey = true)
	public int pkInt;
	@Column(primaryKey = true, maxLength = 10)
	public String pkString;

	// Integer
	@Column(nullable = true, defaultValue = "12")
	public int primitiveInt;

	@Column(name = "nullInt2", nullable = true)
	public Integer nullInt;

	@Column(nullable = false)
	public Integer nonNullInt;

	@Column(nullable = true, readonly = true, defaultValue = "defaultTest")
	public String readOnlyString;

	// String
	public String nullString;
	@Column(nullable = false, maxLength = 15)
	public String nonNullString;

	// BigDecimal
	public BigDecimal nullBigDecimal;
	@Column(nullable = false, defaultValue = "123.4556")
	public BigDecimal nonNullBigDecimal;

	// Date
	public Date nullDate;

	@Column(nullable = false, defaultValue = "20130221T151617")
	public Date nonNullDate;

	// Boolean
	@Column(defaultValue = "true")
	public boolean nonNullBoolean;
	public Boolean nullBoolean;

	// Byte
	@Column(defaultValue = "127")
	public byte nonNullByte;
	public Byte nullByte;

	// Char
	@Column(defaultValue = "z")
	public char nonNullChar;
	public Character nullChar;

	// Long
	@Column(defaultValue = "34359738368")
	public long nonNullLong;
	public Long nullLong;

	// Double
	@Column(defaultValue = "123.432")
	public double nonNullDouble;
	public Double nullDouble;

	// Float
	@Column(defaultValue = "12.45")
	public float nonNullFloat;
	public Float nullFloat;

	// Short
	@Column(defaultValue = "1234")
	public short nonNullShort;
	public Short nullShort;

	// Byte Array
	@Column(defaultValue = "12,-13,45")
	public byte[] byteArray;

	// Enumeration
	@Column(nullable = false, defaultValue = "BOTTOM")
	public Quarks nonNullEnum;

	// Guid
	public UUID nullGuid;
	@Column(nullable = false, defaultValue = "015FF060-8000-48B8-90C4-6330E0A9D8A2")
	public UUID nonNullGuid;

	public void loadSampleData()
	{
		pkInt = 12;
		pkString = "Hi";
		primitiveInt = -13;
		nonNullInt = 123123123;
		nonNullString = "you";
		nonNullBigDecimal = new BigDecimal("1234.5678");
		nonNullDate = new DateTime(2012, 2, 21, 13, 14, 15);
		nonNullBoolean = true;
		nonNullByte = -120;
		nonNullChar = 'c';
		nonNullLong = 124L;
		nonNullDouble = 234.234;
		nonNullFloat = 987.02983f;
		nonNullShort = 4545;
		byteArray = new byte[] { 1, 2, 3, 4, 5 };
		nonNullEnum = Quarks.STRANGE;
		nonNullGuid = UUID.randomUUID();
		readOnlyString = "hidden";
	}

}
