package cs.dto.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiErrorResponse {
	private List<String>errors;
	private HttpStatus status;
	private String message;
	private List<GuiViolation> violations;
	private int code;

	private String timeStamp;
	private String trace;
	private String correlationId;
	private String requestPath;

	public GuiErrorResponse(int status, Map<String, Object> errorAttributes) {
		init();
		this.status = HttpStatus.NOT_ACCEPTABLE;
		errors.add((String)errorAttributes.get("error"));
		this.message = (String) errorAttributes.get("message");
		this.timeStamp = errorAttributes.get("timestamp").toString();
		this.trace = (String) errorAttributes.get("trace");
		this.requestPath = (String) errorAttributes.get("path");
	}

	private void init()
	{
		if (errors == null) errors = new ArrayList<String>();
		if (violations == null) violations = new ArrayList<GuiViolation>();
	}

	public void addViolations(List<GuiViolation>violations)
	{
		init();
		for(GuiViolation violation : violations)
		{
			errors.add(violation.toString());
			this.violations.add(violation);
		}
	}

	public void addViolation(GuiViolation violation)
	{
		init();
		errors.add(violation.toString());
		this.violations.add(violation);
	}

	public GuiErrorResponse(HttpStatus status, String message, List<GuiViolation>violations) {
		super();
		this.status = status;
		this.message = message;
		addViolations(violations);
	}

	public GuiErrorResponse(HttpStatus status, String message, GuiViolation violation) {
		super();
		this.status = status;
		this.message = message;
		addViolation(violation);
	}

	public GuiErrorResponse(HttpStatus status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public void setTrace(StackTraceElement[] stackTrace) {
		StringBuilder trace = new StringBuilder();
		for (StackTraceElement line : stackTrace)
		{
			trace.append(line.toString());
		}
	}
}
