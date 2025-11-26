package hxc.utils.asn1.generator;

public class ASN1Specification
{

	private ASN1Module modules[];

	public ASN1Module[] getModules()
	{
		return modules;
	}

	public ASN1Module getModule(String moduleName)
	{
		for (ASN1Module module : modules)
		{
			if (module.getIdentifier().equals(moduleName))
				return module;
		}
		return null;
	}

	public void setModules(ASN1Module modules[])
	{
		this.modules = modules;
	}

}
