package hxc.connectors.ctrl.protocol;

public class PingRequest extends ServerRequest
{

	private static final long serialVersionUID = -2567829154938979779L;
	private int seq;

	public void setSeq(int seq)
	{
		this.seq = seq;
	}

	public int getSeq()
	{
		return seq;
	}
}
