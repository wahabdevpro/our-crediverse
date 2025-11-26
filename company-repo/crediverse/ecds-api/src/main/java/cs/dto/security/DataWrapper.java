package cs.dto.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataWrapper
{
	private EncryptionKey auth;
	private String data;
	private String error;
	private boolean coauth;
	private String parentUuid;
}
