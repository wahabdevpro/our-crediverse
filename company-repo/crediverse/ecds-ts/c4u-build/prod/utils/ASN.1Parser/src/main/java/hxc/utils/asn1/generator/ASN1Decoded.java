package hxc.utils.asn1.generator;

import java.util.LinkedHashMap;

public class ASN1Decoded
{

	private LinkedHashMap<String, ASN1Descriptor<String>> fields;

	public ASN1Decoded(LinkedHashMap<String, ASN1Descriptor<String>> fields)
	{
		this.fields = fields;
	}

	public LinkedHashMap<String, ASN1Descriptor<String>> getFields()
	{
		return fields;
	}

	public void setFields(LinkedHashMap<String, ASN1Descriptor<String>> fields)
	{
		this.fields = fields;
	}

}
