package hxc.connectors.cai;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaiResponseParser
{
	final static Logger logger = LoggerFactory.getLogger(CaiResponseParser.class);
	
	public void parseStream(PeekBufferedReader reader, CaiResponse response)
	{		
		String responseCode = "";
		String content = "";
		StringBuilder sb = new StringBuilder();
		char tokenRESP[] = {'R','E','S','P',':'};
		int readResult = 0;
		char cbuf[] = new char[1];
		int state = 0;
		try {
			while(readResult != -1)
			{					
				// wait and block until we can read 
				readResult = reader.read(cbuf);
				char c = (char)cbuf[0];
				if(c == tokenRESP[state])
				{					
					sb.append(c);
					if(state == 4 && c == ':')
					{
						responseCode = eatResponseCode(reader);
						response.setResponseCode(responseCode); 
						readResult = reader.read(cbuf);
						c = cbuf[0];
						if(c == ':')
						{
							content = eatContent(reader);
							response.setContent(content);
						}
						eatWhitespaces(reader);
						logger.debug("CAI : parseStream : response code = [{}] content = [{}]", responseCode, content);							
						return;
					} else {
						state++; //maybe we have received a RESP:####; or RESP:####:KEY,VALUE:KEY,VALUE;
					}
				} else {
					state = 0; //reset because we received a garbage character from prompt or other?
				}
			}
		} catch (IOException e) {			
			logger.error("CAI : parseStream : Socket network exception occurred while attempting to parse CAI response", e);
		}
		return;
	}
	
	private String eatResponseCode(PeekBufferedReader reader) throws IOException
	{
		int character = 0;
		StringBuilder sb = new StringBuilder();
		while(character != -1)
		{
			if(reader.ready())
			{
				character = reader.peek();
				if(character != -1)
				{
					char c = (char)character;
					if(c >= '0' && c <= '9' )
					{
						sb.append(c);
						reader.read();
					} else if( c == ';' || c == ':')
					{						
						return sb.toString();
					} else {
						return null;
					}
				}
			} else {
				return null;
			}
		}
		return null;
	}
	
	private String eatContent(PeekBufferedReader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int character = 0;
		while(character != -1)
		{
			if(reader.ready())
			{
				character = reader.read();
				if(character != -1)
				{
					char c = (char)character;
					sb.append(c);
					if(c == ';')
					{
						return sb.toString();
					}
				}
			}
		}
		return null;
	}
	
	private void eatWhitespaces(PeekBufferedReader reader) throws IOException
	{
		int character = 0;
		while(character != -1)
		{
			if(reader.ready())
			{
				character = reader.peek();
				if(character != -1)
				{
					char c = (char)character;
					if(c == '\n' || c == '\r')
					{
						character = reader.read();
					} else {
						return;
					}
				}
			}
		}
	}
	
	public void clearStream(PeekBufferedReader reader) 
	{
		try
		{
			int character = 0;
			while(character != -1)
			{
				if(reader.ready())
				{
					character = reader.read();					
				} else {
					return;
				}
			}
		}
		catch(IOException e)
		{
			logger.error("CAI : clearBuffer : Socket network exception occurred while clearing command prompts from the stream. {}", e.getMessage());
		}
	}
}

