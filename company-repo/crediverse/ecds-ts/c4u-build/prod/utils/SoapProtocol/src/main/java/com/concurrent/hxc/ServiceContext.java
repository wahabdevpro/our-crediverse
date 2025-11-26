package com.concurrent.hxc;

import hxc.connectors.soap.ISubscriber;

public class ServiceContext implements IServiceContext
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected ISubscriber subscriber;
	protected Object properties;
	protected String resultText;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ISubscriber getSubscriberProxy()
	{
		return subscriber;
	}

	@Override
	public void setSubscriberProxy(ISubscriber subscriber)
	{
		this.subscriber = subscriber;
	}

	@Override
	public Object getProperties()
	{
		return properties;
	}

	@Override
	public void setProperties(Object properties)
	{
		this.properties = properties;
	}

	@Override
	public String getRestultText()
	{
		return resultText;
	}

	@Override
	public void setResultText(String resultText)
	{
		this.resultText = resultText;
	}

}
