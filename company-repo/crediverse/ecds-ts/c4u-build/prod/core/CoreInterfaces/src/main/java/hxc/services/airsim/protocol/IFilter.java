package hxc.services.airsim.protocol;

public interface IFilter
{

	public abstract String getField();

	public abstract String getValue();

	public abstract Comparator getComparator();

}
