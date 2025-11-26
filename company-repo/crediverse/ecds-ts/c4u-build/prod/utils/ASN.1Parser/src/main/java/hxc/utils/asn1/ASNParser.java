package hxc.utils.asn1;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;

import hxc.utils.asn1.generator.ASN1Decoded;
import hxc.utils.asn1.generator.ASN1Descriptor;
import hxc.utils.asn1.generator.ASN1Module;
import hxc.utils.asn1.generator.ASN1Specification;

public abstract class ASNParser
{
	protected ASN1InputStream asn1InputStream;
	private ASN1Specification specification;

	public ASNParser()
	{

	}

	public ASN1Specification complieSpecification(String asn1Specification) throws IOException
	{
		return complieSpecification(new FileInputStream(asn1Specification));
	}

	public ASN1Specification complieSpecification(FileInputStream stream) throws IOException
	{
		ASN1Reader reader = new ASN1Reader(stream);
		try
		{
			reader.analyse();
		}
		catch (Exception e)
		{
			return null;
		}
		return specification = reader.dumpSpecification();
	}

	public boolean hasSpecification()
	{
		return specification != null;
	}

	public List<ASN1Decoded> decode(String filename, String moduleName) throws Exception
	{
		return decode(filename, moduleName, ALL_RECORDS);
	}

	public abstract List<ASN1Decoded> decode(String filename, String moduleName, int record) throws Exception;

	protected ASN1Decoded decodePrimitive(ASN1Primitive primitive, String moduleName) throws Exception
	{
		if (specification == null)
			return null;

		if (primitive instanceof ASN1Sequence)
		{
			List<ASN1Descriptor<ASN1Object>> values = new ArrayList<>();
			enumerateASNObject(values, primitive);

			return new ASN1Decoded(iterateTags(values, moduleName));

		}

		return null;
	}

	private LinkedHashMap<String, ASN1Descriptor<String>> iterateTags(List<ASN1Descriptor<ASN1Object>> descriptors, String moduleName)
	{
		LinkedHashMap<String, ASN1Descriptor<String>> result = new LinkedHashMap<>();
		for (ASN1Descriptor<ASN1Object> d : descriptors)
		{
			try
			{
				ASN1Module module = specification.getModule(moduleName);
				String elementName = null;
				for (Integer i : d.getTag())
				{
					if (module.getElements() != null && module.getElements()[0].isMultiple())
						module = module.getElements()[0].getModule();

					elementName = module.getElements()[i].getName();
					module = module.getElements()[i].getModule();
				}

				try
				{
					String value = new String(d.getASN1().getEncoded());
					value = value.replaceAll("\\n|\\t|\\s|\\r", "").trim();
					if (value.length() == 0)
						value = d.getASN1().toString().replace("#", "");

					result.put(elementName, new ASN1Descriptor<String>(value, d.getTag()));
				}
				catch (IOException e)
				{
				}
			}
			catch (Exception e)
			{
			}
		}

		return result;
	}

	private void enumerateSequence(List<ASN1Descriptor<ASN1Object>> values, ASN1Sequence sequence) throws Exception
	{
		Enumeration<?> e = sequence.getObjects();
		while (e.hasMoreElements())
		{
			Object obj = e.nextElement();
			if (!enumerateASNObject(values, obj))
			{
				throw new Exception("Unknown ASN1 Element. Element: " + obj.toString());
			}
		}
	}

	private void enumerateSet(List<ASN1Descriptor<ASN1Object>> values, ASN1Set set) throws Exception
	{
		Enumeration<?> e = set.getObjects();
		while (e.hasMoreElements())
		{
			Object obj = e.nextElement();
			if (!enumerateASNObject(values, obj))
			{
				throw new Exception("Unknown ASN1 Element. Element: " + obj.toString());
			}
		}
	}

	private List<Integer> tag = new LinkedList<>();

	private void enumerateASN1TaggedObject(List<ASN1Descriptor<ASN1Object>> values, ASN1TaggedObject taggedObject) throws Exception
	{
		tag.add(taggedObject.getTagNo());
		ASN1Primitive primitive = taggedObject.getObject();
		if (!enumerateASNObject(values, primitive))
		{
			ASN1Descriptor<ASN1Object> asnDescriptor = new ASN1Descriptor<ASN1Object>(primitive, tag);
			values.add(asnDescriptor);

			tag = new LinkedList<>(tag);
		}
		tag.remove(tag.size() - 1);
	}

	private boolean enumerateASNObject(List<ASN1Descriptor<ASN1Object>> values, Object object) throws Exception
	{
		if (object instanceof ASN1Sequence)
		{
			enumerateSequence(values, (ASN1Sequence) object);
		}
		else if (object instanceof ASN1Set)
		{
			enumerateSet(values, (ASN1Set) object);
		}
		else if (object instanceof ASN1TaggedObject)
		{
			enumerateASN1TaggedObject(values, (ASN1TaggedObject) object);
		}
		else
		{
			return false;
		}

		return true;
	}

	public static final int ALL_RECORDS = -1;
	public static final int FIRST_RECORD = -2;
	public static final int LAST_RECORD = -3;
}
