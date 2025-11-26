package hxc.utils.instrumentation;

import java.util.Date;

public interface IListener
{
	public abstract void receive(IMetric metric, Date timeStamp, Object... values);
}
