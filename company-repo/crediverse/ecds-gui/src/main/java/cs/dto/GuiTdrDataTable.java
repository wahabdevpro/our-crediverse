package cs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(Include.ALWAYS)
public class GuiTdrDataTable
{
	private GuiTransactionEx[] data;
	private String draw;
	private int recordsTotal;
	private int recordsFiltered;

	public <T> GuiTdrDataTable(GuiTransactionEx[] data)
	{
		this.data = data;
		this.recordsTotal = data.length;
		this.recordsFiltered = data.length;
	}
}
