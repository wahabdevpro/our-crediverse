package cs.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiCosignInfo
{
	private String coSignatorySessionID;
	private String coSignatoryTransactionID;
	private String coSignatoryOTP;
	private String coSignForSessionID;
}
