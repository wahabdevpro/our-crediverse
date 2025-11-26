package cs.security.permissions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EcdsPermissionException extends Exception implements Response.StatusType
{

	@Getter
	private String reasonPhrase = "UNAUTHORIZED";
	private Status status;

	public EcdsPermissionException(String message, Object... args)
	{
		super( String.format(message, args) );
		status = Status.FORBIDDEN;
	}

	@Override
	public int getStatusCode()
	{
		return status.getStatusCode();
	}

	@Override
	public Family getFamily()
	{
		return Response.Status.Family.CLIENT_ERROR;
	}

}
