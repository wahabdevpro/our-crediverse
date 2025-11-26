package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class ConfigurableErrorParam implements IConfigurableParam
{

	private static final long serialVersionUID = 4107322940606101263L;
	private String fieldName;
	private Object value; // Null if (configurable != null)
	private String error; // Error parameter

	@Override
	public String getFieldName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFieldName(String name)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(Object value)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @return the error
	 */
	public String getError()
	{
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(String error)
	{
		this.error = error;
	}

	@Override
	public int compareTo(IConfigurableParam other)
	{
		return this.fieldName.compareTo(other.getFieldName());
	}

}
