package cs.dto;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Area;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiArea extends Area {

	private String parentAreaName;

	public GuiArea(){}

	public GuiArea(String id)
	{
		this.id = Integer.parseInt(id);
	}

	public GuiArea(Area orig)
	{
		BeanUtils.copyProperties(orig, this);
	}

}
