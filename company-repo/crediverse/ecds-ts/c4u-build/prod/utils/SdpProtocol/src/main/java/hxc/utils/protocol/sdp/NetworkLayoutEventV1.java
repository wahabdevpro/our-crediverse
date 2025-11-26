package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

public class NetworkLayoutEventV1 extends FileDTO
{

	@CsvField(column = 0, format = "yyyyMMdd'T'HHmmss")
	public Date timestamp;

	@CsvField(column = 1)
	public int mobileCountryCode;

	@CsvField(column = 2)
	public int mobileNetworkCode;

	@CsvField(column = 3)
	public int locationAreaCode;

	@CsvField(column = 4)
	public int cellId;

	@CsvField(column = 5, optional = true)
	public double longitude;

	@CsvField(column = 6, optional = true)
	public double latitude;

	@CsvField(column = 7)
	public String streetAddress;

}
