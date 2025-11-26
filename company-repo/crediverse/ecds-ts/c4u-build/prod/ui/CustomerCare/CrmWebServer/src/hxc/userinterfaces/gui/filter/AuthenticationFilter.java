package hxc.userinterfaces.gui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/AuthenticationFilter")
public class AuthenticationFilter implements Filter
{

	private ServletContext context;

	@Override
	public void init(FilterConfig fConfig) throws ServletException
	{
		this.context = fConfig.getServletContext();
		this.context.log("AuthenticationFilter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		// HttpServletRequest req = (HttpServletRequest) request;
		// HttpServletResponse res = (HttpServletResponse) response;
		//
		// String uri = req.getRequestURI();
		// this.context.log("Requested Resource::"+uri);
		//
		// HttpSession session = req.getSession(false);
		//
		// if (session != null && uri.endsWith("/logout"))
		// {
		// session.invalidate();
		// session.setAttribute("user", null);
		// request.getRequestDispatcher("/login").forward(request, response);
		// res.sendRedirect("/login");
		// }
		// else
		// {
		// chain.doFilter(request, response);
		// }
	}

	@Override
	public void destroy()
	{
		// close any resources here
	}

}
