package cs.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseRequest
{
	String cid;
	byte[] data;
	String uuid;
	String parentUuid;

	public void setData(String str)
	{
		data = str.getBytes();
	}

	public String getData()
	{
		return new String(data);
	}

	@JsonIgnore
	public void setDataBytes(byte[] bt)
	{
		data = bt;
	}

	@JsonIgnore
	public byte[] getDataBytes()
	{
		return data;
	}
}
