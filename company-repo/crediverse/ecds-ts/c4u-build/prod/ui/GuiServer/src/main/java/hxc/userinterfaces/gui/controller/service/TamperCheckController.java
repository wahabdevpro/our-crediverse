package hxc.userinterfaces.gui.controller.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperCheckRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperResetRequest;
import hxc.utils.protocol.uiconnector.response.GetEcdsCheckTamperedAgentResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperCheckResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperResetResponse;


public class TamperCheckController implements IThymeleafController
{
	
	//public static final String AIRSIM_USAGELIST = "airusagerecords"; 

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);		
		String page = null;
		String json = null;

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		User user = (User) session.getAttribute("user");
		
		// Dialog parameters
		String action = (String) request.getParameter("act");
		if (action != null)
		{		
			String name = (String) request.getParameter("name");
			String msisdn = (String) request.getParameter("msisdn");
			if (name != null)
			{
				switch(action)
				{
					case "reset":
						json = resetTamperedRecords(ctx, user, name);						
						break;
					case "refresh":
						getTamperedRecords(ctx, user, name);
						break;
					case "checkAgent":
						json = getTamperedAgent(ctx, user, msisdn);
						break;
					case "resetAgent":						
						json = resetTamperedRecord(ctx, user, "agent", msisdn);
						break;
					case "resetAccount":
						json = resetTamperedRecord(ctx, user, "account", msisdn);
						break;
					
				}			
				if(name.equals("auditentry"))
				{							
					page = "ecds/tamperedauditentries";
				} 
				else if(name.equals("batch"))
				{							
					page = "ecds/tamperedbatches";
				}
			}
		}		
		if (page == null)
			sendResponse(response, json);
		else
			templateEngine.process(page, ctx, response.getWriter());
	}
	
	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}
	
	private void getTamperedRecords(WebContext ctx, User user, String entity)
	{
		GetEcdsTamperCheckRequest.Entity ent = GetEcdsTamperCheckRequest.Entity.fromCode(entity);
		GetEcdsTamperCheckResponse tamperCheck = UiConnectionClient.getInstance().getEcdsTamperCheck(user, ent);	
		switch(ent)
		{
		case AGENT:
			ctx.setVariable("tamperedAgents", tamperCheck.getTamperedAgents());
			break;
		case ACCOUNT:
			ctx.setVariable("tamperedAccounts", tamperCheck.getTamperedAccounts());
			break;
		case AUDITENTRY:
			ctx.setVariable("tamperedAuditEntries", tamperCheck.getTamperedAuditEntries());
			break;
		case BATCH:
			ctx.setVariable("tamperedBatches", tamperCheck.getTamperedBatches());
			break;
		default:
			break;
		}
	}
	
	private String resetTamperedRecords(WebContext ctx, User user, String entity)
	{
		GetEcdsTamperResetRequest.Entity ent =  GetEcdsTamperResetRequest.Entity.fromCode(entity);
		GetEcdsTamperResetResponse resetResponse = UiConnectionClient.getInstance().resetEcdsTamperedRecords(user, ent);
		GuiUpdateResponse response;
		if(resetResponse.getResult() == true)
		{
			response = new GuiUpdateResponse(OperationStatus.pass, "Resetting " + entity.toString() +  " done.");
		} else {
			response = new GuiUpdateResponse(OperationStatus.fail, "Resetting " + entity.toString() +  " failed.");
		}
		getTamperedRecords(ctx, user, entity);
		return response.toString(); 
	}
	
	public String getTamperedAgent(WebContext ctx, User user, String msisdn)
	{
		GetEcdsCheckTamperedAgentResponse tamperCheck = UiConnectionClient.getInstance().checkTamperedAgent(user, msisdn);
		String agentResult = tamperCheck.getAgentTampered()?"Agent: TAMPERED":"Agent: OK";
		String accountResult = tamperCheck.getAccountTampered()?"Account: TAMPERED":"Account: OK";
		GuiUpdateResponse response = new GuiUpdateResponse(OperationStatus.pass, agentResult + "; " + accountResult); 
		return response.toString();
	}
	
	public String resetTamperedRecord(WebContext ctx, User user, String entity, String msisdn)
	{
		GetEcdsTamperResetRequest.Entity ent =  GetEcdsTamperResetRequest.Entity.fromCode(entity);
		GetEcdsTamperResetResponse resetResponse = UiConnectionClient.getInstance().resetEcdsTamperedRecord(user, ent, msisdn);
		GuiUpdateResponse response;
		if(resetResponse.getResult() == true)
		{
			response = new GuiUpdateResponse(OperationStatus.pass, "Resetting " + entity.toString() +  " done.");
		} else {
			response = new GuiUpdateResponse(OperationStatus.fail, "Resetting " + entity.toString() +  " failed.");
		}
		return response.toString();
	}
}
