package hxc.connectors.cai;

import java.util.HashMap;

public class CaiResponse 
{
	String responseCode;
	String content;
	HashMap<String, String> values = new HashMap<String, String>();
	
	public CaiResponse()
	{
		this.responseCode = "";
		this.content = "";
	}
	
	public CaiResponse(String responseCode, String content)
	{
		this.responseCode = responseCode;
		this.content = content;
	}
	
	String getResponseCode()
	{
		return this.responseCode;
	}
	
	void setResponseCode(String responseCode)
	{
		this.responseCode = responseCode;
	}
	
	String getContent()
	{
		return content;
	}
	
	String getResponseDescription()
	{
		if(!this.responseCode.isEmpty())
		{
			if(this.responseCode.compareTo("0") == 0)
			{
				return "Successful";
			} if(this.responseCode.compareTo("2001") == 0)
			{
				return "Multi Activation database error";
			} if(this.responseCode.compareTo("2002") == 0)
			{
				return "Internal Multi Activation error";
			} if(this.responseCode.compareTo("3001") == 0)
			{
				return "Invalid command";
			} if(this.responseCode.compareTo("3004") == 0)
			{
				return "Insufficient parameters";
			} if(this.responseCode.compareTo("3006") == 0)
			{
				return "User ID/password invalid or expired";
			} if(this.responseCode.compareTo("3007") == 0)
			{
				return "User ID/password invalid or expired";
			} if(this.responseCode.compareTo("3008") == 0)
			{
				return "Invalid command sequence";
			}
			return "Unknown/non-documented error";
		} else {
			return null;
		}
	}
	
	void setContent(String content)
	{
		this.content = content;
		String[] fields = content.split("(:|;)");
		if(fields.length >= 2) // at least msisdn and one other...
		{
			for(int i = 0; i < fields.length; i++)
			{
				String[] kvp = fields[i].split(",");
				if(kvp.length >= 2)
				{
					String key = kvp[0];
					String value = kvp[1];
					values.put(key, value);
				} 						
			}
		}
	}
	
	public void setReturnValue(String key, String value)
	{
		values.put(key, value);
	}
	
	public String getReturnValue(String key)
	{
		return values.get(key);
	}
	
	public boolean containsKey(String key)
	{
		return values.containsKey(key);
	}
}
