package cs.dto.error;

import java.util.List;

import hxc.ecds.protocol.rest.Violation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class GuiValidationException extends Exception {

	public GuiValidationException(String string) {
		super(string);
	}
	
	public GuiValidationException(String string, Throwable th) {
		super(string+" "+th.toString());
		this.setStackTrace(th.getStackTrace());
	}

	public GuiValidationException(List<Violation> violations) {
		super(violations.toString());
		this.violations = violations;
	}
	
	public GuiValidationException(List<Violation> violations, String message) {
		super(message);
		this.violations = violations;
	}

	private static final long serialVersionUID = 1L;
	
	private List<Violation>violations;
	@Setter
	private String correlationId;
}
