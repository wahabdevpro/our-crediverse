package hxc.services.ecds.grizzly.utils;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.IOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuxConnectionProbe implements ConnectionProbe
{
	final static Logger logger = LoggerFactory.getLogger(HuxConnectionProbe.class);
	final GrizzlyConnectionSnapshot connectionSnapshot;
	
	public HuxConnectionProbe(GrizzlyConnectionSnapshot connectionSnapshot)
	{
		this.connectionSnapshot = connectionSnapshot;
	}
	
	@Override
	public void onBindEvent(Connection connection) 
	{
		connectionSnapshot.incrementBindEventCount();
	}

	@Override
	public void onAcceptEvent(Connection serverConnection, Connection clientConnection) 
	{
		connectionSnapshot.incrementAcceptEventCount();
	}

	@Override
	public void onConnectEvent(Connection connection) {
		connectionSnapshot.incrementConnectEventCount();
		connectionSnapshot.incrementOpenConnectionCount();
	}

	@Override
	public void onReadEvent(Connection connection, Buffer data, int size){}

	@Override
	public void onWriteEvent(Connection connection, Buffer data, long size){}

	@Override
	public void onErrorEvent(Connection connection, Throwable error) 
	{
		connectionSnapshot.incrementErrorEventCount();
	}

	@Override
	public void onCloseEvent(Connection connection)
	{
		connectionSnapshot.incrementCloseEventCount();
		connectionSnapshot.decrementOpenConnectionCount();
	}

	@Override
	public void onIOEventReadyEvent(Connection connection, IOEvent ioEvent){}

	@Override
	public void onIOEventEnableEvent(Connection connection, IOEvent ioEvent){}

	@Override
	public void onIOEventDisableEvent(Connection connection, IOEvent ioEvent){}
}
