package hxc.userinterfaces.gui.controller.components;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.services.notification.IPhrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

/**
 * Builds data structure for Page for ResultsCodes
 * @author John Eatwell
 */
public class ResultCodeController implements IThymeleafController
{
	
	/*
	 * JSON Formatting (old format)
	 * 
	 * { "rc" : [ { "rc" : "success", "texts" : [ "", "", "", ...] },...
	 */

	private final Logger logger = Logger.getLogger(this.getClass().toString());
	
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		String json = null;
		try
		{
			ReturnCodeTextDefaultsResponse resp = UiConnectionClient.getInstance().getDefaultResponseCodes(user);
			GetLocaleInformationResponse locale = UiConnectionClient.getInstance().extractLocaleInformation(user);
			
			JsonObject job = new JsonObject();	// Main Object to send

			/*
			 * JSON Data structure:
			 * { 
			 * 		"langs" : ["eng", "fre"],
			 * 		"codes" : {
			 * 			"success" : ["English message", "French message"],
			 * 			"suspended" : ["English message", "French message"]
			 * 		}
			 * }
			 */
			
			// Get list of languages configured (Alphabetically arranged + Unique) - Build "langs"
			Set<String> langCodes = new TreeSet<>();
			for (int langId = 0; langId < IPhrase.MAX_LANGUAGES; langId++)
			{
				String langCode = locale.getLanguage(langId);
				if (langCode != null && langCode.length() > 0)
				{
					langCodes.add(langCode);
				}
			}
			JsonArray jlangs = new JsonArray();
			for (String langCode : langCodes)
			{
				jlangs.add(new JsonPrimitive( langCode ));
			}
			job.add("langs", jlangs);
			
			// Build list of defaults ("codes")
			JsonObject codes = new JsonObject();
			for (ReturnCodeTexts rct : resp.getDefaultReturnTexts())
			{
				// Build Texts for the code
				JsonArray txts = new JsonArray();
				for (String langCode : langCodes)
				{
					String text = rct.getPhrase().get(langCode);
					if (text == null)
						text = "";
					txts.add(new JsonPrimitive(text));
				}
				codes.add(rct.getReturnCode().toString(), txts);
			}
			job.add("codes", codes);
			json = job.toString();
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, String.format("Error thrown in ResultCodeController, retrieving default ResultsCodes: %s", e.getMessage()), e);
		}

		if (json == null)
		{
			json = (new GuiUpdateResponse(OperationStatus.fail, "Failed to retrieve return codes")).toString();
		}

		GuiUtils.sendResponse(response, json);
	}
}
