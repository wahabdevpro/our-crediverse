package hxc.services.airsim.protocol;

public class UssdResponse implements IUssdResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String text;
	private boolean last;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@Override
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public boolean isLast()
	{
		return last;
	}

	public void setLast(boolean last)
	{
		this.last = last;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UssdResponse()
	{

	}

	public UssdResponse(String text, boolean last)
	{

	}

	public UssdResponse(IUssdResponse response)
	{
		this.text = response.getText();
		this.last = response.isLast();
	}

}
