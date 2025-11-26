package hxc.connectors.hsx;

import java.io.IOException;

import hxc.connectors.IConnection;
import hxc.connectors.sms.ISmsConnector;
import hxc.utils.protocol.hsx.DeliverSMRequest;

public class HsxConnection implements IConnection
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnection Implementation
	//
	// /////////////////////////////////
	@Override
	public void close() throws IOException
	{
	}

}
