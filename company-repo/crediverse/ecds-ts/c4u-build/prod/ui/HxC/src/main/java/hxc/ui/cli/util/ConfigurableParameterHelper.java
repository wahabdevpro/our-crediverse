package hxc.ui.cli.util;

import java.util.Set;
import java.util.TreeSet;

import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.notification.Texts;
import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.system.Pair;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public class ConfigurableParameterHelper
{
	public Set<String> extractLocale(CLIConnector connector, String userName, String sessionID) throws Exception
	{
		GetLocaleInformationResponse localeInfo = connector.extractLocaleInformation(userName, sessionID);
		Set<String> langCodes = new TreeSet<>();
		for(String lang : localeInfo.getLanguages())
		{
			if (lang != null && lang.length() > 0)
				langCodes.add(lang);
		}
		return langCodes;
	}
	
	public IConfigurableParam extractFieldConfiguration(Configurable config, String parmName) throws Exception
	{
		if (config != null)
		{
			int index = -1;
			for (int i = 0; i < config.getParams().length; i++)
			{
				if (config.getParams()[i].getFieldName().equalsIgnoreCase(parmName))
				{
					index = i;
					break;
				}
			}
			
			if (index >= 0)
			{
				return config.getParams()[index];
			}
		}
		
		return null;
	}
	
	/**
	 * Create Phrase Header 
	 */
	public String phraseHeader(Set<String> langCodes, String fieldName)
	{
		StringBuilder sb = new StringBuilder();
		for(String langCode : langCodes)
		{
			if (sb.length() > 0)
				sb.append(",");
			
			sb.append( fieldName + "_" + langCode);
		}
		return sb.toString();
	}
	
	public String phraseContent(Set<String> langCodes, IPhrase phrase)
	{
		StringBuilder sb = new StringBuilder();
		for(String langCode : langCodes)
		{
			if (sb.length() > 0)
				sb.append(",");
			
			if (phrase.get(langCode) == null)
			{
				sb.append( "NULL" );
			}
			else
			{
				sb.append( String.format("\"%s\"", phrase.get(langCode)) );	
			}
		}
		return sb.toString();
	}
	
	public Object configurableResponseParamToBaseValue(String type)
	{
		Object value = null;
		if (type.equalsIgnoreCase("int"))
			value = Integer.valueOf(0);
		else if (type.equalsIgnoreCase("boolean"))
			value = Boolean.valueOf(false);
		else if (type.equalsIgnoreCase("long"))
			value = Long.valueOf(0);
		else if (type.equalsIgnoreCase("byte"))
			value = Byte.valueOf((byte) 0);
		else if (type.equalsIgnoreCase("float"))
			value = Float.valueOf(0f);
		else if (type.equalsIgnoreCase("double"))
			value = Double.valueOf(0D);
		else if (type.equalsIgnoreCase("hxc.services.notification.Texts"))
			value = new Texts();
		else if (type.equalsIgnoreCase("java.lang.String"))
			value = new String();
		else if (type.equalsIgnoreCase("java.lang.Integer"))
			value = Integer.valueOf(0);
		else if (type.equalsIgnoreCase("hxc.utils.calendar.TimeUnits"))
			value = new String();
		else if (type.equalsIgnoreCase("[I"))
			value = new int[0];
		else if (type.equalsIgnoreCase("[Ljava.lang.Integer;"))
			value = new Integer[0];
		else if (type.equalsIgnoreCase("[Ljava.lang.String;"))
			value = new String[0];
		else if (type.equalsIgnoreCase("hxc.services.notification.Phrase"))
			value = new Phrase();
		else if (type.equalsIgnoreCase("hxc.services.notification.ReturnCodeTexts"))
			value = new ReturnCodeTexts();
		else
		{
			value = "";
		}
		return value;
	}
	
	public BasicConfigurableParm[] createFieldSetToPopulate(ConfigurableResponseParam conParm)
	{
		BasicConfigurableParm[] fields = new BasicConfigurableParm[conParm.getStructure().length];
		for (int i = 0; i < fields.length; i++)
		{
			fields[i] = new BasicConfigurableParm(conParm.getStructure()[i].getFieldName());
			Object value = configurableResponseParamToBaseValue(conParm.getStructure()[i].getValueType());
			fields[i].setValue(value);
		}
		return fields;
	}
	
	/**
	 *	Extracts Pair<first = fieldName, second = languageCode> 
	 */
	public Pair<String, String> extractFieldAndLanguguage(String header)
	{
		String fieldName = header;
		String languageCode = null;
		
        if ((header.indexOf('_') > 0) && ((header.length() - header.lastIndexOf('_')) == 4))
        {
            fieldName = header.substring(0, header.lastIndexOf('_'));
        	languageCode = header.substring(header.lastIndexOf('_') + 1);
        }
		
		return new Pair<>(fieldName, languageCode);
	}
	
	/**
	 * Find field Index Number 
	 */
	public int findFieldIndex(IConfigurableParam[] fields, String fieldName)
	{
		for(int index = 0; index < fields.length; index++)
		{
			if (fields[index].getFieldName().equalsIgnoreCase(fieldName))
				return index;
		}
		return -1;
	}
	
	public void populateField(IConfigurableParam[] fields, int fieldIndex, String value)
	{
		if (fields[fieldIndex].getValue() instanceof String)
			fields[fieldIndex].setValue(value);
		else if (fields[fieldIndex].getValue() instanceof Integer)
			fields[fieldIndex].setValue(Integer.parseInt(value));
		else if (fields[fieldIndex].getValue() instanceof Boolean)
			fields[fieldIndex].setValue(Boolean.parseBoolean(value));
		else if (fields[fieldIndex].getValue() instanceof Long)
			fields[fieldIndex].setValue(Long.parseLong(value));
		else if (fields[fieldIndex].getValue() instanceof Byte)
			fields[fieldIndex].setValue(Byte.parseByte(value));
		else if (fields[fieldIndex].getValue() instanceof Float)
			fields[fieldIndex].setValue(Float.parseFloat(value));
		else if (fields[fieldIndex].getValue() instanceof Double)
			fields[fieldIndex].setValue(Double.parseDouble(value));
		
	}

}
