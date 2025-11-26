package cs.dto;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.dto.security.EncryptionKey;
import hxc.ecds.protocol.rest.ChangePasswordResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiChangePasswordResponse extends ChangePasswordResponse{

	@Autowired
	private ObjectMapper mapper;

	private EncryptionKey auth;
	private String data;
	private String exponent;
	private String modulus;

	public GuiChangePasswordResponse()
	{
	}

	public GuiChangePasswordResponse(ChangePasswordResponse other) throws JsonProcessingException
	{
		this.setData(mapper.writeValueAsString(other.getKey()));
	}

}
