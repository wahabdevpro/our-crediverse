package hxc.utils.protocol.uiconnector.response;

import java.io.Serializable;

import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class ConfigurableResponseParam implements Serializable, IConfigurableParam
{
	private static final long serialVersionUID = 6490681564449154668L;

	// Required for Value
	private String fieldName;
	private Object value; // each value will need to be serializable

	// Required for structure
	private boolean readOnly;
	// private Class<?> valueType; // Class().getName()
	private String valueType; // String value of Class().getName()

	// Annotations
	private String description;
	private String comment;
	private String defaultValue;
	private String renderAs;
	private int maxLength = -1;
	private String minValue;
	private String maxValue;
	private String[] possibleValues;
	private boolean hidden; // If true then gui will not display to edit this field
	private String group;
	private boolean unique;
	private String referencesKey;

	private int scaleFactor = 1;
	private int decimalDigitsToDisplay = 0;

	// This is just a test
	private ConfigurableResponseParam[] structure; // Used to define structure when object array required

	public ConfigurableResponseParam()
	{
	}

	public ConfigurableResponseParam(String fieldName)
	{
		this.fieldName = fieldName;
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

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * @param readOnly
	 *            the readOnly to set
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	public String getValueType()
	{
		return valueType;
	}

	public void setValueType(String valueType)
	{
		this.valueType = valueType;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description)
	{
		if (description != null && description.length() > 0)
		{
			this.description = description;
		}
	}
	
	/**
	 * @return the description
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setComment(String comment)
	{
		if (comment != null && comment.length() > 0)
		{
			this.comment = comment;
		}
	}	

	/**
	 * @return the renderAs
	 */
	public String getRenderAs()
	{
		return renderAs;
	}

	/**
	 * @param renderAs
	 *            the renderAs to set
	 */
	public void setRenderAs(String renderAs)
	{
		this.renderAs = renderAs;
	}

	/**
	 * @return the maxLength
	 */
	public int getMaxLength()
	{
		return maxLength;
	}

	/**
	 * @param maxLength
	 *            the maxLength to set
	 */
	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * @return the minValue
	 */
	public String getMinValue()
	{
		return minValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(String minValue)
	{
		this.minValue = minValue;
	}

	/**
	 * @return the maxValue
	 */
	public String getMaxValue()
	{
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(String maxValue)
	{
		this.maxValue = maxValue;
	}

	/**
	 * @return the possibleValues
	 */
	public String[] getPossibleValues()
	{
		return possibleValues;
	}

	/**
	 * @param possibleValues
	 *            the possibleValues to set
	 */
	public void setPossibleValues(String[] possibleValues)
	{
		this.possibleValues = possibleValues;
	}

	@Override
	public int compareTo(IConfigurableParam other)
	{
		return this.fieldName.compareTo(other.getFieldName());
	}

	/**
	 * @return the structure
	 */
	public ConfigurableResponseParam[] getStructure()
	{
		return structure;
	}

	/**
	 * @param structure
	 *            the structure to set
	 */
	public void setStructure(ConfigurableResponseParam[] structure)
	{
		this.structure = structure;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden()
	{
		return hidden;
	}

	/**
	 * @param hidden
	 *            the hidden to set
	 */
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	/**
	 * @return the scaleFactor
	 */
	public int getScaleFactor()
	{
		return scaleFactor;
	}

	/**
	 * @param scaleFactor
	 *            the scaleFactor to set
	 */
	public void setScaleFactor(int scaleFactor)
	{
		this.scaleFactor = scaleFactor;
	}

	/**
	 * @return the decimalDigitsToDisplay
	 */
	public int getDecimalDigitsToDisplay()
	{
		return decimalDigitsToDisplay;
	}

	/**
	 * @param decimalDigitsToDisplay
	 *            the decimalDigitsToDisplay to set
	 */
	public void setDecimalDigitsToDisplay(int decimalDigitsToDisplay)
	{
		this.decimalDigitsToDisplay = decimalDigitsToDisplay;
	}

	/**
	 * @return the group
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(String group)
	{
		this.group = group;
	}

	public boolean isUnique()
	{
		return unique;
	}

	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	public String getReferencesKey()
	{
		return referencesKey;
	}

	public void setReferencesKey(String referencesKey)
	{
		this.referencesKey = referencesKey;
	}

}
