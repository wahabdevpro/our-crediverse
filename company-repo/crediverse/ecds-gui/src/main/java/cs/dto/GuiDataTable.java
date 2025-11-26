package cs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class GuiDataTable
{
	private Object data;
	private String draw;
	private int recordsTotal;
	private int recordsFiltered;

	@Getter
	@Setter
	@ToString
	public static class TableColumn {
		String data;
		String title;

		public TableColumn(String data, String title)
		{
			this.data = data;
			this.title = title;
		}
	}

	public <T> GuiDataTable(T[] data)
	{
		this.data = data;
		this.recordsTotal = data.length;
		this.recordsFiltered = data.length;
	}

	public <T> GuiDataTable(T[] data, int recordsTotal)
	{
		this.data = data;
		this.recordsTotal = recordsTotal;
		this.recordsFiltered = recordsTotal;
	}

	public <T> GuiDataTable(List<T> data)
	{
		this.data = data;
		this.recordsTotal = data.size();
		this.recordsFiltered = data.size();
	}
}
