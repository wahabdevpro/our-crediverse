package com.concurrent.hxc;

import hxc.connectors.soap.ISubscriber;

public interface IServiceContext
{
	public abstract ISubscriber getSubscriberProxy();

	public abstract void setSubscriberProxy(ISubscriber subscriber);

	public abstract Object getProperties();

	public abstract void setProperties(Object properties);

	public abstract String getRestultText();

	public abstract void setResultText(String resultText);
}
