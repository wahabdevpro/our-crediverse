package hxc.utils.notification;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.services.notification.INotification;

@Table(name = "cf_notification")
public class Notification implements INotification
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public static final int MAX_LANGUAGES = 4;

	private transient Notifications parent;

	@Column(primaryKey = true)
	private long notificationsId;
	@Column(primaryKey = true)
	private int notificationId;
	private Integer parentId;
	@Column(nullable = true)
	private Integer sequenceNo;
	@Column(maxLength = 512, nullable = true)
	private String description;
	@Column(maxLength = 512, nullable = true)
	private String language1Text;
	@Column(maxLength = 512, nullable = true)
	private String language2Text;
	@Column(maxLength = 512, nullable = true)
	private String language3Text;
	@Column(maxLength = 512, nullable = true)
	private String language4Text;

	public long getNotificationsId()
	{
		return notificationsId;
	}

	public void setNotificationsId(long notificationsId)
	{
		this.notificationsId = notificationsId;
	}

	public int getNotificationId()
	{
		return notificationId;
	}

	public void setNotificationId(int notificationId)
	{
		this.notificationId = notificationId;
	}

	public Integer getParentId()
	{
		return parentId;
	}

	public void setParentId(Integer parentId)
	{
		this.parentId = parentId;
	}

	public Integer getSequenceNo()
	{
		return sequenceNo;
	}

	public void setSequenceNo(Integer sequenceNo)
	{
		this.sequenceNo = sequenceNo;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public void setDescription(String description)
	{
		this.description = normalize(description);
	}

	public String getLanguage1Text()
	{
		return language1Text;
	}

	public void setLanguage1Text(String language1Text)
	{
		this.language1Text = language1Text;
	}

	public String getLanguage2Text()
	{
		return language2Text;
	}

	public void setLanguage2Text(String language2Text)
	{
		this.language2Text = language2Text;
	}

	public String getLanguage3Text()
	{
		return language3Text;
	}

	public void setLanguage3Text(String language3Text)
	{
		this.language3Text = language3Text;
	}

	public String getLanguage4Text()
	{
		return language4Text;
	}

	public void setLanguage4Text(String language4Text)
	{
		this.language4Text = language4Text;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Notification(Notifications parent)
	{
		this.parent = parent;
	}

	public Notification()
	{
	}

	public void setParent(Notifications parent)
	{
		this.parent = parent;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// INotification implementation
	//
	// /////////////////////////////////

	@Override
	public String getText(int languageId)
	{
		switch (languageId)
		{
			case 1:
				return getLanguage1Text();
			case 2:
				return getLanguage2Text();
			case 3:
				return getLanguage3Text();
			case 4:
				return getLanguage4Text();
			default:
				return "";
		}
	}

	@Override
	public void setText(int languageId, String text) throws IllegalArgumentException
	{
		switch (languageId)
		{
			case 1:
				setLanguage1Text(normalize(text));
				break;
			case 2:
				setLanguage2Text(normalize(text));
				break;
			case 3:
				setLanguage3Text(normalize(text));
				break;
			case 4:
				setLanguage4Text(normalize(text));
				break;
			default:
				throw new IllegalArgumentException("Invalid languageId");
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private String normalize(String text) throws IllegalArgumentException
	{
		if (text == null)
			return text;

		String[] parts = text.split("(?=\\{)|(?<=\\})");
		StringBuilder builder = new StringBuilder(text.length() << 2);
		for (String part : parts)
		{
			if (part.startsWith("{"))
			{
				String variableName = parent.getVariable(part);
				if (variableName == null)
					throw new IllegalArgumentException(part);
				builder.append(variableName);
			}
			else
				builder.append(part);
		}

		return builder.toString();
	}

}
