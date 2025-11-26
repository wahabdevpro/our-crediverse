package hxc.connectors.cai;

public class CaiInformation implements ICaiInformation
{
	private String imei;

	@Override
	public String getImei() {
		return imei;
	}
	
	public void setImei(String imei)
	{
		this.imei = imei;
	}
}
