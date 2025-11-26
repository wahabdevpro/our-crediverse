package cs.dto;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import hxc.ecds.protocol.rest.ChangePasswordResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiChangePasswordResponse
{
	@Autowired
	private ObjectMapper mapper;
	
	private String returnCode;
	
	public GuiChangePasswordResponse(ChangePasswordResponse response)
	{
		this.returnCode = response.getReturnCode();
	}
}
