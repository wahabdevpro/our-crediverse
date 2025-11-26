package hxc.utils.protocol.uiconnector.common;

public interface IConfigurableParam extends Comparable<IConfigurableParam>
{
	public String getFieldName();

	public void setFieldName(String name);

	public Object getValue();

	public void setValue(Object value);

}
