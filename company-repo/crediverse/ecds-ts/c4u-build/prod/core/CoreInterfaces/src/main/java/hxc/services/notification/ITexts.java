package hxc.services.notification;

import hxc.configuration.Configurable;

@Configurable
public interface ITexts
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	public abstract String getModelText();

	public abstract void setModelText(String modelText);

	public abstract String getText(Integer languageID);

	public abstract void setText(Integer languageID, String text);

	public abstract boolean matches(String text);

	public abstract String getSafeText(Integer languageID);

	public abstract void set(String... texts);
}