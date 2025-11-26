package hxc.userinterfaces.gui.controller.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.configuration.ValidationException;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetResponse;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateResponse;
import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage;
import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage.TimeUnits;
import hxc.utils.protocol.uiconnector.airsim.AirSimSmsResponse;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdResponse;
import hxc.utils.protocol.uiconnector.response.AirSimGetUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStartUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStopUsageResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetAirsimHistoryResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AirSimController implements IThymeleafController
{
	
	public static final String AIRSIM_USAGELIST = "airusagerecords"; 

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		GuiUpdateResponse guiResponse = null;
		String page = null;
		String json = null;

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		User user = (User) session.getAttribute("user");
		
		// Dialog parameters
		String action = (String) request.getParameter("act");
		if (action != null)
		{
			String ind = (String) request.getParameter("index");
			int index = (ind != null) ? Integer.parseInt(ind) : -1;
			String name = (String) request.getParameter("name");
			
			switch(action)
			{
				case "add":
					json = addUsageTimer(session, request, user);
					break;
				case "del":
					if (name != null && name.equals("sms")) 
						json = removeSMSHistory(session, request, user);
					else if (name != null && name.equals("email")) 
						json = removeEmailHistory(session, request, user);
					else
						json = stopUsageTimer(session, request, user);	
					break;
				case "refresh":
					if (name != null && name.equals("sms")) 
					{
						getSMSHistory(ctx, user);
						page = "airsim/smshistory";	
					} 
					else if (name != null && name.equals("email")) 
					{
						getEmailHistory(ctx, user);
						page = "airsim/emailhistory";	
					} 
					else 
					{
						getAirTimerUsage(ctx, user);
						page = "airsim/usagetimerrecords";						
					}
						
					break;
				case "sms":
					json = sendSms(session, request, user);
					break;
				case "ussd":
					json = sendUssd(session, request, user);
					break;
				case "sendAirResponseUpdate":
					json = sendAirResponseUpdate(session, request, user);
					break;
				case "sendAirResponseReset":
					json = sendAirResponseReset(session, request, user);
					break;
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
	
	private String addUsageTimer(HttpSession session, HttpServletRequest request, User user) throws ValidationException
	{
		try
		{
			// Fom Page
			String msisdna = request.getParameter("msisdna").trim();
			String msisdnb = request.getParameter("msisdnb").trim();
			String account = request.getParameter("account");
			String amount = request.getParameter("amount");
			String interval = request.getParameter("interval");
			String timeUnit = request.getParameter("timeUnit");
			String standardDeviation = request.getParameter("standardDeviation");
			String topupValue = request.getParameter("topupValue");
			
			if (msisdnb.length() == 0) msisdnb = msisdna;
			//To Send
			String msisdn = (msisdna.equals(msisdnb))? msisdna : String.format("%s-%s", msisdna, msisdnb);
			int accountNumber = Integer.parseInt(account);
			long amountValue = Long.parseLong(amount);
			long intervalValue = Long.parseLong(interval);
			TimeUnits tu = TimeUnits.valueOf(timeUnit);
			int sdValue = Integer.parseInt(standardDeviation);
			Long topup = topupValue.length()==0? null: Long.parseLong(topupValue);
			
			// Call the back-end and create a GUI response
			AirSimStartUsageResponse response = UiConnectionClient.getInstance().setAirSimStartUsage(user, msisdn, accountNumber, amountValue, intervalValue, tu, sdValue, topup);
			if (response.isStarted())
				return new GuiUpdateResponse(OperationStatus.pass, "Usage Timers created").toString();
			else
				return new GuiUpdateResponse(OperationStatus.fail, "Only some of the Timers might have been created").toString();
			
		} catch(Exception e)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Creating Usage Timers failed. Reason: %s", e.getMessage())).toString();
		}
	}
	
	private String stopUsageTimer(HttpSession session, HttpServletRequest request, User user) throws ValidationException
	{
		try
		{
			// From Page
			String msisdn = request.getParameter("name");
			if (msisdn.equalsIgnoreCase("all"))
				msisdn = null;
			AirSimStopUsageResponse response = UiConnectionClient.getInstance().setAirSimStopUsage(user, msisdn);		
			return new GuiUpdateResponse(OperationStatus.pass, "Usage Timer stopped").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Stop Usage Timers failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	private String removeSMSHistory(HttpSession session, HttpServletRequest request, User user) throws ValidationException
	{
		try
		{
			UiBaseResponse response = UiConnectionClient.getInstance().clearAirSimHistory(user);
			return new GuiUpdateResponse(OperationStatus.pass, "SMS History cleared").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("SMS History clear failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	private String removeEmailHistory(HttpSession session, HttpServletRequest request, User user) throws ValidationException
	{
		try
		{
			UiBaseResponse response = UiConnectionClient.getInstance().clearAirSimEmailHistory(user);
			return new GuiUpdateResponse(OperationStatus.pass, "Email History cleared").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Email History clear failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	/**
	 * Configure information for Usage Table
	 */
	public static void getAirTimerUsage(WebContext ctx, User user)
	{
		// Air MSISDN Usage
		AirSimGetUsageResponse airUsage = UiConnectionClient.getInstance().getAirSimUsage(user);
		if (airUsage == null || airUsage.getAirSimUsage() == null)
			ctx.setVariable("airusagerecords", new ArrayList<>());
		else
		{
			List<AirSimMSISDNUsage> usage = Arrays.asList(airUsage.getAirSimUsage());
			ctx.setVariable("airusagerecords", usage);
		}
	}
	
	private void getSMSHistory(WebContext ctx, User user)
	{
		GetAirsimHistoryResponse airsim = UiConnectionClient.getInstance().getAirsimHistory(user);
		ctx.setVariable("smsHistory", airsim.getSmsHistory());
		ctx.setVariable("emailHistory", airsim.getEmailHistory());
	}
	
	private void getEmailHistory(WebContext ctx, User user)
	{
		GetAirsimHistoryResponse airsim = UiConnectionClient.getInstance().getAirsimHistory(user);
		ctx.setVariable("smsHistory", airsim.getSmsHistory());
		ctx.setVariable("emailHistory", airsim.getEmailHistory());
	}
	
	
	private String sendUssd(HttpSession session, HttpServletRequest request, User user)
	{
		try
		{
			String msisdn = (String) request.getParameter("ussdMsisdn");
			String imsi = (String) request.getParameter("ussdImsi");
			String ussd = (String) request.getParameter("ussdCommand");
			
			UiBaseResponse response = UiConnectionClient.getInstance().sendUssdRequest(user, msisdn, ussd, imsi);
			if (response != null) 
			{
				if (response instanceof AirSimUssdResponse)
				{
					AirSimUssdResponse ussdReponse = (AirSimUssdResponse) response;
					return new GuiUpdateResponse(OperationStatus.pass, ussdReponse.getText()).toString();
				}
				else if (response instanceof ErrorResponse)
				{
					return new GuiUpdateResponse(OperationStatus.fail, String.format("Send USSD Failed with error: %s", ((ErrorResponse) response).getError())).toString();
				}
			}
			 
			return new GuiUpdateResponse(OperationStatus.fail, "Send USSD Failed").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	private String sendSms(HttpSession session, HttpServletRequest request, User user)
	{
		try
		{
			String from = (String) request.getParameter("smsFrom");
			String to = (String) request.getParameter("smsTo");
			String smsText = (String) request.getParameter("smsText");
			
			UiBaseResponse response = UiConnectionClient.getInstance().sendSmsRequest(user, from, to, smsText);
			
			if (response != null) 
			{
				if (response instanceof AirSimSmsResponse)
				{
					return new GuiUpdateResponse(OperationStatus.pass, String.format("SMS Sent from %s to %s, SMS: %s", from, to, smsText)).toString();
				}
				else if (response instanceof ErrorResponse)
				{
					return new GuiUpdateResponse(OperationStatus.fail, String.format("Send SMS Failed with error: %s", ((ErrorResponse) response).getError())).toString();
				}				
			}
			return new GuiUpdateResponse(OperationStatus.fail, "Send SMS Failed").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	private String sendAirResponseUpdate(HttpSession session, HttpServletRequest request, User user)
	{
		try
		{
			String airCall = (String) request.getParameter("airCall");
			String responseCode = (String) request.getParameter("responseCode");
			String delay = (String) request.getParameter("delay");
			
			UiBaseResponse response = UiConnectionClient.getInstance().sendUpdateAirRequest(user, airCall, responseCode, delay);
			
			if (response != null) 
			{
				if (response instanceof AirResponseUpdateResponse)
				{
					return new GuiUpdateResponse(OperationStatus.pass, String.format("Successfully updated AIR call %s to respond with rc=[%s], with delay [%s] millis", airCall, responseCode, delay)).toString();
				}
				else if (response instanceof ErrorResponse)
				{
					return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed to set Air response state: %s", ((ErrorResponse) response).getError())).toString();
				}				
			}
			return new GuiUpdateResponse(OperationStatus.fail, "Updating Air response state failed").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
	private String sendAirResponseReset(HttpSession session, HttpServletRequest request, User user)
	{
		try
		{
			String airCall = (String) request.getParameter("airCall");			
			UiBaseResponse response = UiConnectionClient.getInstance().sendResetAirRequest(user, airCall);
			
			if (response != null) 
			{
				if (response instanceof AirResponseResetResponse)
				{
					return new GuiUpdateResponse(OperationStatus.pass, String.format("Successfully reset AIR call %s to respond with rc=[0], with delay [0] millis", airCall)).toString();
				}
				else if (response instanceof ErrorResponse)
				{
					return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed to reset Air response state: %s", ((ErrorResponse) response).getError())).toString();
				}				
			}
			return new GuiUpdateResponse(OperationStatus.fail, "Resetting Air response state failed").toString();
		}
		catch(Exception ex)
		{
			return new GuiUpdateResponse(OperationStatus.fail, String.format("Failed. Reason: %s", ex.getMessage())).toString();
		}
	}
	
}
