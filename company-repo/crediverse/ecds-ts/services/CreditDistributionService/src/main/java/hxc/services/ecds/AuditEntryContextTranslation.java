package hxc.services.ecds;

public class AuditEntryContextTranslation 
{
	private String headline;
	private String description;
	
	public AuditEntryContextTranslation(String headline, String description)
	{
		this.headline = headline;
		this.description = description;
	}
	
	public String getHeadline()
	{
		return headline;
	}
	
	public void setHeadline(String headline)
	{
		this.headline = headline;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
}
