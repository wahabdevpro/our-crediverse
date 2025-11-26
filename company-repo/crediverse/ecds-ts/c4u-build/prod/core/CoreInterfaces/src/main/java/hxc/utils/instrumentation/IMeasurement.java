package hxc.utils.instrumentation;

import java.util.Date;

public interface IMeasurement extends Runnable, Comparable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract IMetric getMetric();

	public abstract Date getTimeStamp();

	public abstract Object[] getValues();

}