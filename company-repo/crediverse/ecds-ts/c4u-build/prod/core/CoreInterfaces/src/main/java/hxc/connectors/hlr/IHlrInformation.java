package hxc.connectors.hlr;

public interface IHlrInformation
{
	public abstract String getIMSI();
	public abstract Integer getMnpStatus();
	public abstract Integer getMobileCountryCode();
	public abstract Integer  getMobileNetworkCode();
	public abstract Integer  getLocationAreaCode();
	public abstract Integer  getCellIdentity();
}
