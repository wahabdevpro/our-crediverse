package cs.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import cs.dto.error.GuiErrorResponse;

@RestController
public class CustomErrorController implements ErrorController {
	private static Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

	private static final String PATH = "/error";

	@Autowired
	private DefaultErrorAttributes defaultErrorAttributes;
	
	@RequestMapping(PATH)
    public GuiErrorResponse handleError(HttpServletResponse response, WebRequest request) {
        // GuiErrorResponse uses the message and the stack trace, so make sure they are available if possible.
        ErrorAttributeOptions errorOptions = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE,
        														  ErrorAttributeOptions.Include.STACK_TRACE);
        Map<String, Object> errorAttributes = defaultErrorAttributes.getErrorAttributes(request, errorOptions);
        // This is logged as a warning because it occurs for minor missing css issues.  Typical output
        // [WARN] 10:26:14.016 [http-nio-8084-exec-1] cs.controller.CustomErrorController - CustomErrorController called for URL /css/skins/skin-credit.css
        logger.warn("CustomErrorController called for URL "+errorAttributes.get("path"));
        return new GuiErrorResponse(response.getStatus(), errorAttributes);
    }
}
