package cs.dto;

import java.util.List;

import cs.dto.error.GuiViolation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 *	Designed as a simple response messages for REST calls done
 */
public class GuiStatusResponse {
	public enum Status {
		Pass,
		Fail
	}

	private Status status;
	private List<GuiViolation> violations = null;
	private String generalError = null;

	public GuiStatusResponse() {}

	private GuiStatusResponse(Status status, List<GuiViolation> violations, String generalError) {
		this.status = status;
		this.violations = violations;
		this.generalError = generalError;
	}

	public static GuiStatusResponse operationSuccessful()
	{
		return new GuiStatusResponse(Status.Pass, null, null);
	}

	public static GuiStatusResponse validationFailed(List<GuiViolation> violations)
	{
		return new GuiStatusResponse(Status.Fail, violations, null);
	}

	public static GuiStatusResponse transactionServerFailed(String errorMessage)
	{
		return new GuiStatusResponse(Status.Fail, null, errorMessage);
	}

}
