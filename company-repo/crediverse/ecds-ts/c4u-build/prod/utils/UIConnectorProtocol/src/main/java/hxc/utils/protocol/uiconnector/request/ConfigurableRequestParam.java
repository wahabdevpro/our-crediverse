package hxc.utils.protocol.uiconnector.request;

import java.io.Serializable;

import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class ConfigurableRequestParam implements Serializable, IConfigurableParam
{

	private static final long serialVersionUID = 6625951776909533455L;
	private String fieldName;
	private Object value;

	public ConfigurableRequestParam()
	{
	}

	public ConfigurableRequestParam(String fieldName, Object value)
	{
		this.fieldName = fieldName;
		this.value = value;
	}

	/**
	 * @return the fieldName
	 */
	@Override
	public String getFieldName()
	{
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	@Override
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	/**
	 * @return the value
	 */
	@Override
	public Object getValue()
	{
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	@Override
	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public int compareTo(IConfigurableParam other)
	{
		return this.fieldName.compareTo(other.getFieldName());
	}

}
