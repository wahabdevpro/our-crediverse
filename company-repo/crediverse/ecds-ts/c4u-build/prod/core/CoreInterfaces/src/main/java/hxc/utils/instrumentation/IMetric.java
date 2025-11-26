package hxc.utils.instrumentation;

public interface IMetric
{
	public abstract String getName();

	public abstract IDimension[] getDimensions();

	public abstract void subscribe(IListener listener);

	public abstract void unsubscribe(IListener listener);
}
