package cs.dto;

import hxc.ecds.protocol.rest.Permission;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiPermission extends Permission implements Cloneable
{
	private String icon;
	private boolean enabled = false;
	private String textPermission;
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
	
	
}
