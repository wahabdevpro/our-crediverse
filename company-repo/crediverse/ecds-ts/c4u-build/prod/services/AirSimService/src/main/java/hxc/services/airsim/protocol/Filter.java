package hxc.services.airsim.protocol;

public class Filter implements IFilter
{

	private String field;
	private String value;
	private Comparator comparator;

	@Override
	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public Comparator getComparator()
	{
		return comparator;
	}

	public void setComparator(Comparator comparator)
	{
		this.comparator = comparator;
	}

	public Filter()
	{
	}

	public Filter(IFilter filter)
	{
		field = filter.getField();
		value = filter.getValue();
		comparator = filter.getComparator();
	}

}
