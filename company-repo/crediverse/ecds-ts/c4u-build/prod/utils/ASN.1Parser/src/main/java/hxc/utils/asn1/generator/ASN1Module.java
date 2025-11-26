package hxc.utils.asn1.generator;

public class ASN1Module
{

	private String identifier;
	private ASN1Type type;
	private ASN1Element elements[];

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public ASN1Type getType()
	{
		return type;
	}

	public void setType(ASN1Type type)
	{
		this.type = type;
	}

	public ASN1Element[] getElements()
	{
		return elements;
	}

	public void setElements(ASN1Element elements[])
	{
		this.elements = elements;
	}

}
