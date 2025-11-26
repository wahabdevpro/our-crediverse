package hxc.services.numberplan;

public interface INumberPlan
{
	// Conversion
	public abstract String getInternationalFormat(String number);

	public abstract String getNationalFormat(String number);

	public abstract String[] getNationalFormat(String[] numbers);

	// Nature Tests
	public abstract boolean isOnnet(String number);

	public abstract boolean isMobile(String number);

	public abstract boolean isSpecial(String number);

	public abstract boolean isFixed(String number);

	public abstract boolean isNational(String number);

	// Validity Test
	public abstract boolean isValid(String number);

	// Other
	public abstract String getNationalDailingCode();

	public abstract String[] getLegacyOnnetNumberRanges();

}
