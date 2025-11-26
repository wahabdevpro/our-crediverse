package cs.language;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

public class LanguageExceptionHandler extends LocaleChangeInterceptor
{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		try {
			super.preHandle(request, response, handler);
		} catch (Exception e) {
			logger.error("", e);
		}
		return true;
	}

	@Override
	public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale)
	{
		// TODO Auto-generated method stub
		super.setIgnoreInvalidLocale(true);
	}


}
