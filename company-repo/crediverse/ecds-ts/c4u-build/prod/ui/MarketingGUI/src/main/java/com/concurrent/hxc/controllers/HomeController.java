package com.concurrent.hxc.controllers;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.concurrent.hxc.dao.Visitor;
import com.concurrent.hxc.dao.VisitorDAO;
import com.concurrent.hxc.utils.MailHandler;
import com.concurrent.hxc.utils.UssdHandler;

@Controller
public class HomeController
{
	
	private final int MAX_CHARACTERS = 28;
	
	private UssdHandler ussdHandler = new UssdHandler();
	private MailHandler mailHandler = new MailHandler();
	
	@Autowired
	private VisitorDAO visitorDao;
	
	// Loaded when open the page
	@RequestMapping("/")
	public String index(@RequestParam(value = "sid", required = false, defaultValue = "invalid") String sessionId, ModelMap model)
	{
		model.addAttribute("hasSession", true);
		
		// Return the page to startup with
		return "index";
	}
	
	// Returns the string response from the ussd
	@RequestMapping(value = "/simobi", method = RequestMethod.POST)
	@ResponseBody
	public String process(@RequestParam("number") String number, @RequestParam("command") String command)
	{
		// Send the ussd command with the number
		String ussd = ussdHandler.send(number, command);
		
		// Ensure it is valid
		if (ussd != null)
		{
			// Return the ussd response
			return resize(ussd);
			
		}
		
		// Else return invalid command
		return "Invalid Command";
	}
	
	// Sends mail
	@RequestMapping(value = "/mail", method = RequestMethod.POST)
	@ResponseBody
	public String mail(@RequestParam("name") String name, @RequestParam("phone") String phone, @RequestParam("email") String email, @RequestParam("message") String message) throws MessagingException, UnsupportedEncodingException
	{
		// Will always throw exception, need proper credentials
		if (email != null) throw new MessagingException();
	
		// Sends mail to the email
		mailHandler.sendMail(email, "Auto Reply", String.format("Hi %s,<br/><p>We have recieved your message and will contact you soon.</p><br/>Best Regards<br/>Justin Guedes", name));
		
		// Return a success string
		return "Success";
	}
	
	// Indicates a visit to the webpage has happened
	@RequestMapping(value = "/visitor", method = RequestMethod.POST)
	public void visit(@RequestParam("ip") String ip, @RequestParam(value = "country", required = false) String country, @RequestParam(value = "region", required = false) String region, 
					@RequestParam(value = "city", required = false) String city, @RequestParam(value = "postal", required = false) String postal, @RequestParam(value = "location", required = false) String location,
					@RequestParam(value = "isp", required = false) String isp)
	{
		
		// Check if the visitor already exists
		if (visitorDao.getVisitor(ip) != null)
		{
			// Then specifiy that the visitor visited the webpage again
			visitorDao.visit(ip);
			return;
		}
		
		// Else create a visitor
		Visitor visitor = new Visitor();
		
		// Set the visitor information
		visitor.setIp(ip);
		visitor.setCountry(country);
		visitor.setRegion(region);
		visitor.setCity(city);
		visitor.setPostal(postal);
		visitor.setLocation(location);
		visitor.setIsp(isp);
		visitor.setVisits(1);
		
		// Insert the visitor into the database
		visitorDao.upsert(visitor);
		
	}
	
	// Resizes the ussd string to fit in the web simobi
	private String resize(String ussdMenu)
	{
		// Create the string builder
		StringBuilder builder = new StringBuilder();
		
		// Split the ussd menu into an array
		String lines[] = ussdMenu.indexOf('\n') > 0 ? ussdMenu.split("\\n") : new String[] { ussdMenu };
		
		// Iterate through the menu
		for (String line: lines)
		{
			// Check if the line is greater than the number of characters
			if (line.length() > MAX_CHARACTERS)
			{
				// If so, then Get the last space before the max characters has been reached
				int lastSpace = 0;
				for (int i = 0; i < line.length(); i++)
				{
					if (line.charAt(i) == ' ' && i < MAX_CHARACTERS)
					{
						lastSpace = i;
					}
				}
				
				// Add some spaces
				String spaces = "";
				for (int i = 0; i < MAX_CHARACTERS - lastSpace; i++)
				{
					spaces += " ";
				}
				
				// Add the spaces between there so it will go onto the next line
				String temp = line.substring(0, lastSpace) + spaces + line.substring(lastSpace + 1);
				builder.append(temp + '\n');
			}
			else
			{
				// Else just append the line
				builder.append(line + '\n');
			}
		}
		
		// Get the new ussd menu
		String newUssdMenu = builder.toString();
		
		// Return the menu
		return newUssdMenu;
		
	}
	
}
