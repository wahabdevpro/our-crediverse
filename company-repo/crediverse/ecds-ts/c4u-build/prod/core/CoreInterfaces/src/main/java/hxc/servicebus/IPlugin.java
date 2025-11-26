package hxc.servicebus;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.utils.instrumentation.IMetric;

public interface IPlugin
{
	/**
	 * Tells a plug-in to initialise itself but not start
	 * 
	 * @param esb
	 *            reference to the Service Bus
	 */
	public abstract void initialise(IServiceBus esb);

	/**
	 * Tells the plug-in to start but it may refuse in which cast it will be asked later
	 * 
	 * @param args
	 *            arguments from host deamon
	 * @return <code>true</code> if the plug-in managed to start
	 */
	public abstract boolean start(String[] args);

	/**
	 * Tells the plug-in to stop itself
	 */
	public abstract void stop();

	public abstract IConfiguration getConfiguration();

	public abstract void setConfiguration(IConfiguration config) throws ValidationException;

	/**
	 * Asks a Connector/Service weather it can assume the specified role
	 * 
	 * @param serverRole
	 *            .
	 * @return
	 */
	public abstract boolean canAssume(String serverRole);

	/**
	 * Asks a Connector/Service weather it is fit to perform it's duties
	 * 
	 * @return
	 */
	public abstract boolean isFit();

	/**
	 * Asks a Connector/Service weather it is fit to perform it's duties
	 * 
	 * @return
	 */
	public abstract IMetric[] getMetrics();

}
