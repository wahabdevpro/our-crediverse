package hxc.connectors.ctrl.protocol;

import java.io.Serializable;

public class ServerResponse implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String sourceHost;
	private String targetHost;
	private int sequenceNo;
	private static final long serialVersionUID = 3445060746584703251L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getSourceHost()
	{
		return sourceHost;
	}

	public void setSourceHost(String sourceHost)
	{
		this.sourceHost = sourceHost;
	}

	public String getTargetHost()
	{
		return targetHost;
	}

	public void setTargetHost(String targetHost)
	{
		this.targetHost = targetHost;
	}

	public int getSequenceNo()
	{
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo)
	{
		this.sequenceNo = sequenceNo;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public ServerResponse()
	{
	}

	public ServerResponse(ServerRequest request)
	{
		this.sourceHost = request.getTargetHost();
		this.targetHost = request.getSourceHost();
		this.sequenceNo = request.getSequenceNo();
	}

}
