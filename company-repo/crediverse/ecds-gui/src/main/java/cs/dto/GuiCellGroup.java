package cs.dto;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.CellGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiCellGroup extends CellGroup {

	public GuiCellGroup(){}

	public GuiCellGroup(String id)
	{
		this.id = Integer.parseInt(id);
	}

	public GuiCellGroup(CellGroup orig)
	{
		BeanUtils.copyProperties(orig, this);
	}

}
