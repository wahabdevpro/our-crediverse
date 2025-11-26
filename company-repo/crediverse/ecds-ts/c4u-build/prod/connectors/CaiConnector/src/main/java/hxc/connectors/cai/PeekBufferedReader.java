package hxc.connectors.cai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class PeekBufferedReader extends BufferedReader {
	
	//private static int defaultExpectedLineLength = 80;
	private static int defaultCharBufferSize = 8192;
	
	public PeekBufferedReader(Reader in, int sz) {
		super(in, sz);
	}
	
	public PeekBufferedReader(Reader in) {
		this(in, defaultCharBufferSize);
	}
	
	public int peek() throws IOException
	{
		int character = -1;
		if(this.ready())
		{
			this.mark(1);
			character = this.read();
			this.reset();
		}
		return character;
	}	
}
