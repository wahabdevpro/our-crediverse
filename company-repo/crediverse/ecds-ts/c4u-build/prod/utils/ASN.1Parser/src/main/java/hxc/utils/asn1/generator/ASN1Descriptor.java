package hxc.utils.asn1.generator;

import java.util.List;

public class ASN1Descriptor<T>
{
	private T asn1;
	private List<Integer> tag;

	public ASN1Descriptor(T asn1, List<Integer> tag)
	{
		this.asn1 = asn1;
		this.tag = tag;
	}

	public T getASN1()
	{
		return (T) asn1;
	}

	public void setASN1(T asn1)
	{
		this.asn1 = asn1;
	}

	public List<Integer> getTag()
	{
		return tag;
	}

	public void setTag(List<Integer> tag)
	{
		this.tag = tag;
	}

	@Override
	public String toString()
	{
		return String.format("%s %s", asn1.toString(), tag.toString());
	}

}
