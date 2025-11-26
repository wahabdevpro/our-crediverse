package hxc.connectors.diagnostic;

import java.util.LinkedHashMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import hxc.connectors.diagnostic.Field.FieldType;

@XStreamAlias("diagnostics")
public class Diagnostics
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int version;
	@XStreamImplicit
	private LinkedHashMap<String, Field> fields = new LinkedHashMap<String, Field>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public LinkedHashMap<String, Field> getFields()
	{
		return fields;
	}

	public void setFields(LinkedHashMap<String, Field> fields)
	{
		this.fields = fields;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public synchronized Field addField(String scope, String name, FieldType type, int bitLength)
	{
		String key = getKey(scope, name);
		Field field = fields.get(key);
		if (field != null)
			return field;

		Field lastField = null;
		for (Field field2 : fields.values())
		{
			lastField = field2;
		}

		field = new Field();
		field.setScope(scope);
		field.setName(name);
		field.setType(type);
		field.setOffset(lastField == null ? 0 : lastField.getOffset() + lastField.getLength());
		field.setLength(bitLength);
		fields.put(key, field);

		return field;
	}

	private String getKey(String scope, String name)
	{
		return scope + "." + name;
	}

	public Field getField(String scope, String name)
	{
		return fields.get(getKey(scope, name));
	}

}
