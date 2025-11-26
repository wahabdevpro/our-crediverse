package hxc.utils.asn1.ber;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;

import hxc.utils.asn1.ASNParser;
import hxc.utils.asn1.generator.ASN1Decoded;

public class BERDecoder extends ASNParser
{

	private String prevFilename;
	private long prevPosition;

	@Override
	public List<ASN1Decoded> decode(String filename, String moduleName, int record) throws Exception
	{
		if (prevFilename == null || !prevFilename.equals(filename))
		{
			prevFilename = filename;
			prevPosition = 0;
		}

		asn1InputStream = new ASN1InputStream(new FileInputStream(filename));
		List<ASN1Decoded> decoded = new LinkedList<>();

		long available = asn1InputStream.available();
		asn1InputStream.skip(prevPosition);
		prevPosition = available;
		while (asn1InputStream.available() > 0)
		{
			decoded.add(decodePrimitive(asn1InputStream.readObject(), moduleName));
			if (record == FIRST_RECORD)
				break;

		}

		if (record == LAST_RECORD && decoded.size() > 0)
		{
			ASN1Decoded d = decoded.get(decoded.size() - 1);
			decoded = new LinkedList<>();
			decoded.add(d);
		}

		return decoded;
	}

}
