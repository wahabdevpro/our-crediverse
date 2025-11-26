package cs.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiChangePasswordRequest
{
	public enum EntityType{
		WEBUSER,
		AGENT,
		AGENTUSER
	};

	private int entityId;
	private String newPassword;
	private String repeatPassword;
	private String currentPassword;
	private EntityType entityType;

}
