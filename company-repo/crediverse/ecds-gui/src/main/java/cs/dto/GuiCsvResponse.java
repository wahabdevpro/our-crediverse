package cs.dto;

import java.nio.charset.Charset;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiCsvResponse
{
	private String csvData;
	private int recordCount;

	public void setRecordCount(List<String> items)
	{
		recordCount = -1;
		if (items != null && items.size() > 0)
			recordCount = Integer.parseInt(items.get(0));
	}

	public byte[] getBytes()
	{
		return csvData.getBytes(Charset.forName("UTF-8"));
	}

	public byte[] getBytes(boolean skipHeaders)
	{
		if (skipHeaders)
		{
			String truncatedData = csvData.substring(csvData.indexOf('\n') + 1, csvData.length());
			return truncatedData.getBytes(Charset.forName("UTF-8"));
		}

		return csvData.getBytes(Charset.forName("UTF-8"));
	}
}
