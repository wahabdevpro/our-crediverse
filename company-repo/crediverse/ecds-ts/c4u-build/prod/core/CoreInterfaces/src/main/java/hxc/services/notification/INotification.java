package hxc.services.notification;

public interface INotification
{
	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract String getText(int languageID);

	public abstract void setText(int languageID, String text) throws IllegalArgumentException;

}
