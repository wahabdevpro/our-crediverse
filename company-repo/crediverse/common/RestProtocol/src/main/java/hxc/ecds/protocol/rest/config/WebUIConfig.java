package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class WebUIConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 470788161659080239L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;

	protected String sampleSetting = "sample"; // TODO Remove and add real setting fields
	protected int clientStateRetentionDays = 7;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getSampleSetting()
	{
		return sampleSetting;
	}

	public WebUIConfig setSampleSetting(String sampleSetting)
	{
		this.sampleSetting = sampleSetting;
		return this;
	}

	public int getClientStateRetentionDays()
	{
		return clientStateRetentionDays;
	}

	public WebUIConfig setClientStateRetentionDays(int clientStateRetentionDays)
	{
		this.clientStateRetentionDays = clientStateRetentionDays;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConfiguration
	//
	// /////////////////////////////////
	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		WebUIConfig template = new WebUIConfig();
		
		if (sampleSetting == null)
			sampleSetting = template.getSampleSetting();
		
		if (clientStateRetentionDays == 0)
			clientStateRetentionDays = template.getClientStateRetentionDays();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
		// TODO add validation
		;

		return validator.toList();
	}

}
