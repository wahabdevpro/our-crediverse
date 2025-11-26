package hxc.services.subscription;

import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;

public class Variant
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String variantID;
	private int serviceClassID;
	private Phrase names;

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

	public int getServiceClassID()
	{
		return serviceClassID;
	}

	public void setServiceClassID(int serviceClassID)
	{
		this.serviceClassID = serviceClassID;
	}

	public Phrase getNames()
	{
		return names;
	}

	public void setNames(Phrase names)
	{
		this.names = names;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Variant()
	{

	}

	public Variant(String variantID, int serviceClassID, IPhrase names)
	{
		this.variantID = variantID;
		this.serviceClassID = serviceClassID;
		this.names = new Phrase(names);
	}

	public static Variant getVariant(Variant[] variants, String variantID)
	{
		if (variants == null || variantID == null)
			return null;

		for (Variant variant : variants)
		{
			if (variant.matches(variantID))
				return variant;
		}

		return null;
	}

	private boolean matches(String variantID)
	{
		if (variantID == null)
			return false;

		return this.variantID.equalsIgnoreCase(variantID) || names.matches(variantID);
	}

}
