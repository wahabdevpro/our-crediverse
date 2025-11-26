package hxc.utils.protocol.uiconnector.response;

public class ESBShutdownResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 7757238733739290341L;

	private int pid;

	public void setPID(int pid)
	{
		this.pid = pid;
	}

	public int getPID()
	{
		return this.pid;
	}
}
