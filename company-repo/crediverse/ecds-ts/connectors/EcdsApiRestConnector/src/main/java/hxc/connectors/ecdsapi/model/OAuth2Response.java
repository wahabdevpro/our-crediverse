package hxc.connectors.ecdsapi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OAuth2Response {
	private String access_token;
	private String token_type;
	private int expires_in;
	private String scope;
	private String jti;
	
	@JsonIgnore
	private final static Logger logger = LoggerFactory.getLogger(OAuth2Response.class);
	
	public String getAccess_token() 
	{
		return access_token;
	}
	
	public void setAccess_token(String access_token) 
	{
		this.access_token = access_token;
	}
	
	public String getToken_type() 
	{
		return token_type;
	}
	
	public void setToken_type(String token_type) 
	{
		this.token_type = token_type;
	}
	
	public int getExpires_in() 
	{
		return expires_in;
	}
	
	public void setExpires_in(int expires_in) 
	{
		this.expires_in = expires_in;
	}
	
	public String getScope() 
	{
		return scope;
	}
	
	public void setScope(String scope) 
	{
		this.scope = scope;
	}
	
	public String getJti() 
	{
		return jti;
	}
	
	public void setJti(String jti) 
	{
		this.jti = jti;
	}
}
