package hxc.connectors.ecdsapi.model;

import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import hxc.connectors.ecdsapi.utils.UnixTimestampDeserializer;

public class JsonWebTokenPayload 
{
	ArrayList<String> scope;
	@JsonDeserialize(using = UnixTimestampDeserializer.class)
	Date exp;
	ArrayList<String> authorities;
	String jti;
	String client_id;

	@JsonCreator
    public JsonWebTokenPayload(@JsonProperty("scope") ArrayList<String> scope,
    		@JsonProperty("exp") Date exp,
    		@JsonProperty("authorities") ArrayList<String> authorities,
    		@JsonProperty("jti") String jti,
    		@JsonProperty("client_id") String client_id) 
	{
		this.scope = scope;
        this.exp = exp;
        this.authorities = authorities;
        this.jti = jti;
        this.client_id = client_id;
    }
	
	public ArrayList<String> getScope() 
	{
		return scope;
	}
	
	public void setScope(ArrayList<String> scope) 
	{
		this.scope = scope;
	}
	
	public Date getExp() 
	{
		return exp;
	}
	
	public void setExp(Date exp) 
	{
		this.exp = exp;
	}
	
	public ArrayList<String> getAuthorities() 
	{
		return authorities;
	}
	
	public void setAuthorities(ArrayList<String> authorities) 
	{
		this.authorities = authorities;
	}
	
	public String getJti() 
	{
		return jti;
	}
	
	public void setJti(String jti) 
	{
		this.jti = jti;
	}
	
	public String getClient_id() 
	{
		return client_id;
	}
	
	public void setClient_id(String client_id) 
	{
		this.client_id = client_id;
	}
}
