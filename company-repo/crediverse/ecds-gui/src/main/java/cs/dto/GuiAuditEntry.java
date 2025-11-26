package cs.dto;

import java.text.SimpleDateFormat;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.AuditEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiAuditEntry extends AuditEntry
{
	public enum UserTypeEnum {
		WEBUSER("W"),
		AGENT("A"),
		NONE("-");
		private String val;
		private UserTypeEnum(String val) {
			this.val = val.toUpperCase();
		}

		public String getVal(String val)
		{
			return this.val;
		}

		public static UserTypeEnum fromString(String val)
		{
			UserTypeEnum result = NONE;
			if (val != null)
			{
				switch (val)
				{
					case "W":
						result = WEBUSER;
						break;
					case "A":
						result = AGENT;
						break;
				}
			}
			return result;
		}
	}

	protected String timestampString;
	protected String userName;
	protected UserTypeEnum userType;

	public GuiAuditEntry()
	{
		super();
	}

	public GuiAuditEntry(AuditEntry orig)
	{
		BeanUtils.copyProperties(orig, this);

		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		if (this.timestamp != null)
			this.timestampString = ft.format(this.timestamp);
	}

	public AuditEntry getAuditEntry()
	{
		AuditEntry entry = new AuditEntry();
		BeanUtils.copyProperties(this, entry);
		return entry;
	}
}
