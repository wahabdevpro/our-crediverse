package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.utils.protocol.caisim.response.soap.SoapResponse;

public class ImplBase<TResponse extends SoapResponse>
{
	protected ICaiData caiData;
	
	public ImplBase(ICaiData caiData)
	{
		this.caiData = caiData;
	}
	
	protected TResponse exitWith(TResponse response, int responseCode)
	{
		response.setResponseCode(responseCode);
		return response;
	}
}