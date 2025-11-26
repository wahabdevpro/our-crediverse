package hxc.services.pin;

import hxc.configuration.Configurable;
import hxc.services.notification.Phrase;

@Configurable
public class Variant
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// ID of this Variant
	private String variantID;

	// Name of the PIN Service Variant
	private Phrase name;

	// Maximum number of Retries
	private int maxRetries;

	// Minimum Length
	private int minLength;

	// Maximum Length
	private int maxLength;

	// Default
	private String defaultPin;

	// May use Default PIN to Transact
	private boolean mayUseDefault;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public Phrase getName()
	{
		return name;
	}

	public void setName(Phrase name)
	{
		this.name = name;
	}

	public int getMaxRetries()
	{
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries)
	{
		this.maxRetries = maxRetries;
	}

	public int getMinLength()
	{
		return minLength;
	}

	public void setMinLength(int minLength)
	{
		this.minLength = minLength;
	}

	public int getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public String getDefaultPin()
	{
		return defaultPin;
	}

	public void setDefaultPin(String defaultPin)
	{
		this.defaultPin = defaultPin;
	}

	public boolean isMayUseDefault()
	{
		return mayUseDefault;
	}

	public void setMayUseDefault(boolean mayUseDefault)
	{
		this.mayUseDefault = mayUseDefault;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Variant()
	{

	}

	public Variant(String variantID, Phrase name, //
			int maxRetries, int minLength, int maxLength, String defaultPin, boolean mayUseDefault)
	{
		super();
		this.variantID = variantID;
		this.name = name;
		this.maxRetries = maxRetries;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.defaultPin = defaultPin;
		this.mayUseDefault = mayUseDefault;
	}

}
