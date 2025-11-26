package hxc.connectors.ctrl.protocol;

public class PingResponse extends ServerResponse
{

	private static final long serialVersionUID = 8934136487750873856L;
	private int seq;

	public void setSeq(int seq)
	{
		this.seq = seq;
	}

	public int getSeq()
	{
		return seq;
	}

	public PingResponse(PingRequest request)
	{
		super(request);
		this.seq = request.getSeq() + 1;
	}
}
