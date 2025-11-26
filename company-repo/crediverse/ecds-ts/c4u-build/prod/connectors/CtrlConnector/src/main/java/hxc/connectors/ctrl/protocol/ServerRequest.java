package hxc.connectors.ctrl.protocol;

import java.io.Serializable;

public class ServerRequest implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String sourceHost;
	private String targetHost;
	private int sequenceNo;
	private static final long serialVersionUID = -9172121733669070589L;

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

}
