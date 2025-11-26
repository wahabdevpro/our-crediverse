package cs.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Cell;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiCell
{
	private int id;
	private int mobileCountryCode;
	private int mobileNetworkCode;
	private int localAreaCode;
	private int cellID;
	private java.lang.Double longitude;
	private java.lang.Double latitude;
	private List<GuiArea> areas = new ArrayList<GuiArea>();
	private List<GuiCellGroup> cellGroups = new ArrayList<GuiCellGroup>();

	public GuiCell(){}

	public GuiCell(Cell cell)
	{
		BeanUtils.copyProperties(cell, this);
		this.setAreas(new ArrayList<GuiArea>(cell.getAreas().size()));
		for (int i = 0; i < cell.getAreas().size(); i++) {
			GuiArea guiArea = new GuiArea();
			BeanUtils.copyProperties(cell.getAreas().get(i), guiArea);
			this.getAreas().add(guiArea);
		}
		this.setCellGroups(new ArrayList<GuiCellGroup>(cell.getCellGroups().size()));
		for (int i = 0; i < cell.getCellGroups().size(); i++) {
			GuiCellGroup guiCellGroup = new GuiCellGroup();
			BeanUtils.copyProperties(cell.getCellGroups().get(i), guiCellGroup);
			this.getCellGroups().add(guiCellGroup);
		}
	}
}
