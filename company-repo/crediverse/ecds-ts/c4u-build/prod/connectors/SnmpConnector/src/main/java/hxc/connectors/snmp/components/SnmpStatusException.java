package hxc.connectors.snmp.components;

// An easy way to create more meaningful error messages
public class SnmpStatusException extends Exception
{

	private static final long serialVersionUID = 65453629513520500L;

	enum State
	{
		notFound, couldNotSend, unknown
	};

	private State state;
	private String error = "Could not determine the error that has occurred.";

	public SnmpStatusException(State state)
	{
		this.state = state;
	}

	public int getState()
	{
		return state.ordinal();
	}

	@Override
	public String getMessage()
	{
		switch (state)
		{
			case notFound:
				error = "Could not find the OID or Name in the Mib Database";
				break;

			case couldNotSend:
				error = "Could not send the Snmp Trap to the NMC";
				break;

			case unknown:
				error = "Unknown error has occurred.";
				break;
		}
		return error;
	}
}
