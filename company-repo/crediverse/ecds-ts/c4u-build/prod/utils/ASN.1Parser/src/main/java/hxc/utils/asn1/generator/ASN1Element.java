package hxc.utils.asn1.generator;

public class ASN1Element
{

	private String name;
	private ASN1Module module;
	private ASN1ElementConstraint constraint;
	private boolean multiple = false;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ASN1Module getModule()
	{
		return module;
	}

	public void setModule(ASN1Module module)
	{
		this.module = module;
	}

	public ASN1ElementConstraint getConstraint()
	{
		return constraint;
	}

	public void setConstraint(ASN1ElementConstraint constraint)
	{
		this.constraint = constraint;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

}
