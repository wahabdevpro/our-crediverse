package cs.dto.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiUserRole {
	private int id;
	private String name;
	
	public GuiUserRole(){}
	public GuiUserRole(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
}
