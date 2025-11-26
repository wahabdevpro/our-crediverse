package cs.dto.error;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiGeneralException extends Exception
{
	private String correlationId;
	private HttpStatus errorCode;
	private String additional;
	private String serverCode;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<String>errors;
	private Map<String, Object>headers;

	public GuiGeneralException(String string) {
		super(string);
	}

	public GuiGeneralException(String string, Throwable th) {
		super(string+" "+th.toString());
		this.setStackTrace(th.getStackTrace());
	}
}
