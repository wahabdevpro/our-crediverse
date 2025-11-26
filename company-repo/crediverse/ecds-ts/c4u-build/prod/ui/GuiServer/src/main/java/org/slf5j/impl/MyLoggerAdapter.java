package org.slf5j.impl;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import hxc.services.logging.LoggingLevels;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;

public class MyLoggerAdapter implements Logger
{

	public MyLoggerAdapter()
	{
	}

	public MyLoggerAdapter(String name)
	{
	}

	@Override
	public void debug(String arg0)
	{

	}

	@Override
	public void debug(String arg0, Object arg1)
	{
	}

	@Override
	public void debug(String arg0, Object[] arg1)
	{

	}

	@Override
	public void debug(String arg0, Throwable arg1)
	{
	}

	@Override
	public void debug(Marker arg0, String arg1)
	{
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2)
	{
	}

	@Override
	public void debug(Marker arg0, String arg1, Object arg2)
	{
	}

	@Override
	public void debug(Marker arg0, String arg1, Object[] arg2)
	{
	}

	@Override
	public void debug(Marker arg0, String arg1, Throwable arg2)
	{
	}

	@Override
	public void debug(Marker arg0, String arg1, Object arg2, Object arg3)
	{
	}

	@Override
	public void error(String arg0)
	{
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, arg0);
	}

	@Override
	public void error(String arg0, Object arg1)
	{
		String msg = MessageFormatter.format(arg0, arg1).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public void error(String arg0, Object[] arg1)
	{
		String msg = MessageFormatter.format(arg0, arg1).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public void error(String arg0, Throwable arg1)
	{
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, arg1.getLocalizedMessage());
	}

	@Override
	public void error(Marker arg0, String arg1)
	{
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, arg0.toString());
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2)
	{
		StringBuilder sb = new StringBuilder(MessageFormatter.format(arg0, arg1).getMessage());
		sb.append(arg2.toString());
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, sb.toString());
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2)
	{
		String msg = MessageFormatter.format(arg1, arg2).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public void error(Marker arg0, String arg1, Object[] arg2)
	{
		String msg = MessageFormatter.format(arg1, arg2).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public void error(Marker arg0, String arg1, Throwable arg2)
	{
		String msg = MessageFormatter.format(arg1, arg2).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2, Object arg3)
	{
		String msg = MessageFormatter.format(arg1, arg2, arg3).getMessage();
		UiConnectionClient.getInstance().log(LoggingLevels.ERROR, msg);
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void info(String arg0)
	{
	}

	@Override
	public void info(String arg0, Object arg1)
	{
	}

	@Override
	public void info(String arg0, Object[] arg1)
	{

	}

	@Override
	public void info(String arg0, Throwable arg1)
	{
	}

	@Override
	public void info(Marker arg0, String arg1)
	{
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2)
	{
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2)
	{
	}

	@Override
	public void info(Marker arg0, String arg1, Object[] arg2)
	{
	}

	@Override
	public void info(Marker arg0, String arg1, Throwable arg2)
	{
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2, Object arg3)
	{
	}

	@Override
	public boolean isDebugEnabled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDebugEnabled(Marker arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isErrorEnabled()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isErrorEnabled(Marker arg0)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isInfoEnabled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInfoEnabled(Marker arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTraceEnabled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTraceEnabled(Marker arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWarnEnabled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWarnEnabled(Marker arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void trace(String arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String arg0, Object arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String arg0, Object[] arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String arg0, Throwable arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(Marker arg0, String arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(Marker arg0, String arg1, Object[] arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(Marker arg0, String arg1, Throwable arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2, Object arg3)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String arg0, Object arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String arg0, Object[] arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String arg0, Throwable arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Marker arg0, String arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Marker arg0, String arg1, Object[] arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Marker arg0, String arg1, Throwable arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2, Object arg3)
	{
		// TODO Auto-generated method stub

	}
	// Bunch of inherited methods here. Let your IDE generate this.
	// Implement these methods to do your own logging.
}