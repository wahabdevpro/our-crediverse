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
	private String currentPassword;
	private String repeatPassword;
	private String data;
	private EntityType entityType;
	
}