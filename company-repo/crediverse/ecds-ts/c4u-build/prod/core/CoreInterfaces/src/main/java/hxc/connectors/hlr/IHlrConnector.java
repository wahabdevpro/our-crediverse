package hxc.connectors.hlr;

public interface IHlrConnector
{
	public abstract IHlrInformation getInformation(String msisdn, boolean needLocation, boolean needMnp, boolean needImsi);
}
