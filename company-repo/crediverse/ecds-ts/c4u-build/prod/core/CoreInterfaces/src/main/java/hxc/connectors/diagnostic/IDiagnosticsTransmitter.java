package hxc.connectors.diagnostic;

import java.util.Date;

public interface IDiagnosticsTransmitter
{
	public abstract void set(String scope, String name, boolean value);

	public abstract void set(String scope, String name, String value, int maxLength);

	public abstract void set(String scope, String name, int value, int bitLength);

	public abstract void set(String scope, String name, Date value);
}
