package hxc.services.caisim.engine.cai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.caisim.ICaiData;
import hxc.utils.tcp.TcpResponse;

/**
 * A class to handle DUMMY commands - for use in testing.
 * 
 * @author petar
 *
 */
public class DummyHandler extends BaseHandler
{
	final static Logger logger = LoggerFactory.getLogger(DummyHandler.class);
	
	public DummyHandler(ICaiData caiData)
	{
		super(caiData);
	}
	
	public TcpResponse handleCai(String[] parsedRequest)
	{
		return getSuccessResponse();
	}
}
