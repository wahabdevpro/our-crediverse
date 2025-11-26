package cs.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.header.HeaderWriter;

public class CustomHeaderWriter implements HeaderWriter
{

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession(false);
		if (session != null)
		{
			if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid())
			{
				response.setHeader("X-Login", "Required");
			}
		}
		else
		{
			response.setHeader("X-Login", "Required");
		}
	}

}
