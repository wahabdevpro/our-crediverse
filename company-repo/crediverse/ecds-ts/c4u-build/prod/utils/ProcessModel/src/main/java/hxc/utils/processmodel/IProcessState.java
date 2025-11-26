package hxc.utils.processmodel;

import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;

import hxc.connectors.vas.VasService;
import hxc.servicebus.ILocale;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.INotifications;

public interface IProcessState extends IServiceContext
{
	public abstract Action getCurrentAction();

	public abstract void setCurrentAction(Action action);

	public abstract void setCompleted(boolean completed);

	public abstract String getInput();

	public abstract void setInput(String input);

	public abstract String getOutput();

	public abstract void setOutput(String output);

	public abstract IHxC getVasInterface();

	public abstract void setVasInterface(IHxC vasInterface);

	public abstract VasService getVasService();

	public abstract void setVasService(VasService vasService);

	public abstract VasService getVasService(String serviceID);

	public abstract ReturnCodes getLastReturnCode();

	public abstract void setLastReturnCode(ReturnCodes returnCode);

	public abstract <T> void set(Action action, String variableName, T value);

	public abstract <T> T get(Action action, String variableName);

	public abstract int getLanguageID();

	public abstract String getLanguageCode();

	public abstract INotifications getNotifications();

	public abstract int getMaxOutputLength();

	public abstract ILocale getLocale();

	public abstract Number getSubscriberNumber();

	public abstract <T extends RequestHeader> T getRequest(Class<T> cls);

}
