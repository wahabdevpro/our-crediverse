package hxc.utils.protocol.caisim;

/**
 * Represents a list of requests for PDP context deletion.
 * 
 * @see DeletedPdpContext
 * @author petar
 *
 */
public class DeletedPdpContexts
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private DeletedPdpContext[] deletedPdps = new DeletedPdpContext[0];
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public DeletedPdpContexts()
	{
		
	}
	
	public DeletedPdpContexts(DeletedPdpContext[] deletedPdps)
	{
		this.deletedPdps = deletedPdps;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////
	
	public DeletedPdpContext[] getPdpContext()
	{
		return deletedPdps;
	}

	public void setPdpContext(DeletedPdpContext[] deletedPdps)
	{
		this.deletedPdps = deletedPdps;
	}
}
