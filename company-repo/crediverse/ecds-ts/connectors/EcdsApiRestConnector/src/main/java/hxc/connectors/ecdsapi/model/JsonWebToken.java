package hxc.connectors.ecdsapi.model;

import org.glassfish.jersey.internal.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonWebToken {
	String accessToken; //Source of truth
	JsonWebTokenHeader header; //Populated from accessToken by process()
	JsonWebTokenPayload payload; //Populated from accessToken by process()

	public JsonWebToken(String accessToken) throws Exception
	{
		this.accessToken = accessToken;
		process();
	}
	
	public JsonWebTokenHeader getHeader() 
	{
		return header;
	}

	public void setHeader(JsonWebTokenHeader header) 
	{
		this.header = header;
	}

	public JsonWebTokenPayload getPayload() 
	{
		return payload;
	}

	public void setPayload(JsonWebTokenPayload payload) 
	{
		this.payload = payload;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(String accessToken) throws Exception
	{
		this.accessToken = accessToken;
		process();
	}
	
	private void process() throws Exception
	{
		String[] parts = accessToken.split("\\.");
		if(parts.length >= 2)
		{
			String header = Base64.decodeAsString(addBase64Pad(parts[0]));		
			String payload = Base64.decodeAsString(addBase64Pad(parts[1]));
			ObjectMapper mapper = new ObjectMapper();
			JsonWebTokenHeader jwtHeader = mapper.readValue(header, JsonWebTokenHeader.class);
			JsonWebTokenPayload jwtPayload = mapper.readValue(payload, JsonWebTokenPayload.class);
			this.setHeader(jwtHeader);
			this.setPayload(jwtPayload);
		} else {
			String errorMessage = String.format("Invalid JWT access token. Access Token does not contain header, payload and signature fields. %s", accessToken);
			throw new Exception(errorMessage);
		}
	}
	
	private String addBase64Pad(String input)
	{
		int numPads = (3 - input.length() % 3) % 3;
		StringBuilder pads = new StringBuilder(input);
		for(int i = 0; i < numPads; i++) pads.append("=");
		String output = pads.toString();
		return output;
	}
	
}
