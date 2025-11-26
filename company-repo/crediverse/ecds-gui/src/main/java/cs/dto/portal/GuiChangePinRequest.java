package cs.dto.portal;

import hxc.ecds.protocol.rest.ChangePinRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GuiChangePinRequest extends ChangePinRequest
{
	private String repeatPin;

}
