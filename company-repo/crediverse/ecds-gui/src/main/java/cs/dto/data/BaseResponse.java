package cs.dto.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseResponse
{
	String cid;
	String state;
	byte[] data1;
	byte[] data2;
	String error;
	String redirectUrl;
	String uuid;
}
