package hxc.services.ecds.rest.ussd;

import static hxc.services.ecds.util.MsisdnBConfirmationHelper.findConfirmMenuId;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.generateBNumberConfirmationMenu;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.getbNumberConfirmMessage;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.haveToConfirmBNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.UssdMenuButton;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StringExpander;
import hxc.services.logging.LoggingConstants;
import hxc.services.notification.INotificationText;

public abstract class MenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(MenuProcessor.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String CAPTURE_KEY = "?";
	private static final Pattern keyPattern = Pattern.compile("\\[([n]|[0-9*#]+)\\]");
	public static final String B_NUMBER_CONFIRM_MENU_NAME = "bNumberConfirmMenu";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MenuProcessor(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Menu Initiation
	//
	// /////////////////////////////////
	public boolean initiate(EntityManager em, IInteraction interaction, Session session, UssdConfig ussdConfig)
	{
		List<UssdMenu> items = ussdConfig.getMenus();		
		return initiate(em, interaction, session, items, new HashMap<String, String>(), -1);
	}

	public boolean initiate(EntityManager em, IInteraction interaction, Session session, List<UssdMenu> items, Map<String, String> values, int commandID)
	{
		// Store Menu parameters in Session
		MenuState state = new MenuState();
		Map<Integer, UssdMenu> menus = new HashMap<Integer, UssdMenu>();

		if (items != null)
		{
			for (UssdMenu item : items)
			{
				menus.put(item.getId(), item);
			}
		}
		state.menus = menus;
		state.valueMap = values;
		state.commandID = commandID;
		state.menuID = UssdConfig.ROOT_MENU_ID;
		state.offset = 0;
		state.transactionID = (String)MDC.get(LoggingConstants.CONST_LOG_TRANSID);
		//state.transactionID = context.getLogger().getThreadTransactionID(); // FIXME
		session.set(MenuState.PROP_USSD_MENU_STATE, state);
		logger.info("Link to USSD Transaction {}", interaction.getInboundTransactionID());

		// Reply with root menu
		return showMenu(em, session, interaction, state);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Response Processing
	//
	// /////////////////////////////////
	public void processResponse(EntityManager em, IInteraction interaction)
	{
		// Get the Active Session
		Session session = context.getSessions().getActiveAgentSession(em, context, interaction);
		if (session == null)
			return;

		// Test if the Session has expired
		if (session.expired())
		{
			reportError(TransactionsConfig.ERR_SESSION_EXPIRED, interaction, session);
			return;
		}

		// Get State from Session
		MenuState state = session.get(MenuState.PROP_USSD_MENU_STATE);
		if (state == null || state.menus == null || state.menuID == null || state.keyMap == null || state.valueMap == null)
		{
			reportError(TransactionsConfig.ERR_TECHNICAL_PROBLEM, interaction, session);
			return;
		}

		// Restore TS Transaction Number
		MDC.put(LoggingConstants.CONST_LOG_TRANSID, state.transactionID);
		//context.getLogger().setThreadTransactionID(state.transactionID);// FIXME
		logger.info("Continuing USSD Transaction {}", interaction.getInboundTransactionID());

		// Get the Button from the response
		String response = interaction.getMessage();
		UssdMenuButton button = state.keyMap.get(response);
		if (button == null)
			button = state.keyMap.get(CAPTURE_KEY);

		// Redisplay current menu if input is bad
		if (button == null)
		{
			showMenu(em, session, interaction, state);
			return;
		}
		
		// Accumulate options
		state.options |= button.getOptions();

		// Process based on Button Type
		switch (button.getType())
		{
			case UssdMenuButton.TYPE_TEXT:
				// Should not come here
				break;

			case UssdMenuButton.TYPE_NAVIGATE:
				state.menuID = button.getNextMenuID();
				state.offset = 0;
				break;

			case UssdMenuButton.TYPE_CAPTURE:
				state.valueMap.put(button.getCaptureField(), response);
				
				// If the current "button" is for entering recipient MSISDN and confirmation of this MSISDN is enabled
				// in the configuration then ask for confirmation
				IMenuProcessor menuProcessor = getUssdProcessor(button.getCommandID());
				if (haveToConfirmBNumber(menuProcessor, context, button, session, em)) {
					Integer confirmMenuId = findConfirmMenuId(menuProcessor, state.menus);
					if (confirmMenuId == null) {
						confirmMenuId = generateBNumberConfirmationMenu(
							menuProcessor,
							state,
							button,
							getbNumberConfirmMessage(menuProcessor, context, session, em));
					}

					// Navigate to the B number confirmation menu	
					state.menuID = confirmMenuId;
				} else {
					state.menuID = button.getNextMenuID();
				}

				state.offset = 0;
				break;

			case UssdMenuButton.TYPE_OPTION:
				state.valueMap.put(button.getCaptureField(), button.getValue());
				state.menuID = button.getNextMenuID();
				state.offset = 0;
				break;

			case UssdMenuButton.TYPE_COMMAND:
				state.commandID = button.getCommandID();
				state.menuID = button.getNextMenuID();
				state.offset = 0;
				break;

			case UssdMenuButton.TYPE_RESULT:
				// Should not come here
				break;

			case UssdMenuButton.TYPE_EXIT:
				reply(session, interaction, "", false);
				return;

			case UssdMenuButton.TYPE_PREVIOUS:
				state.offset = 0;
				break;

			case UssdMenuButton.TYPE_NEXT:
				state.offset += state.lineCount;
				break;
		}

		showMenu(em, session, interaction, state);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	// /////////////////////////////////
	private boolean showMenu(EntityManager em, final Session session, IInteraction interaction, MenuState state)
	{
		// Non-Existent Menu
		if (state.menus == null)
			return false;

		// Build Menu
		UssdMenu menu = state.menus.get(state.menuID);
		if (menu == null)
			return false;

		// Trim it to fit
		String text = null;
		int skipLines = state.offset;
		int maxLines = Integer.MAX_VALUE;
		boolean nextRequired = false;
		
		CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
		TransactionsConfig transactionsConfig = companyInfo.getConfiguration(em, TransactionsConfig.class);

		while (maxLines > skipLines)
		{
			// Compose the menu
			List<String> lines = composeMenu(em, session, interaction, state, menu, skipLines, maxLines, nextRequired);
			StringBuilder sb = new StringBuilder();

			// Calculate line count
			state.lineCount = lines.size();
			if (nextRequired)
				state.lineCount--;
			if (skipLines > 0)
				state.lineCount--;

			// Check if it is too long
			for (String line : lines)
			{
				if (sb.length() > 0)
					sb.append('\n');
				sb.append(line);
			}
			text = sb.toString();
			if (text.length() <= transactionsConfig.getMaxUssdLength())
				break;

			// Reduce number of lines if too long
			if (nextRequired)
				maxLines--;
			else
				maxLines = lines.size() - 1;

			nextRequired = true;
		}

		if (text == null || text.isEmpty())
		{
			reportError(TransactionsConfig.ERR_TECHNICAL_PROBLEM, interaction, session);
		}
		else
		{
			reply(session, interaction, text, state.canProceed);
		}

		return true;
	}

	public List<String> composeMenu(EntityManager em, final Session session, IInteraction interaction, //
			MenuState state, UssdMenu menu, int skipLines, int maxLines, boolean nextRequired)
	{
		List<String> result = new ArrayList<String>();
		Moniker moniker = new Moniker();
		boolean previousRequired = skipLines > 0;
		state.canProceed = false;
		
		// String Expander
		final IMenuProcessor stateProcessor = getUssdProcessor(state.commandID);
		StringExpander<Session> expander = new StringExpander<Session>()
		{
			@Override
			protected String expandField(String englishName, Locale locale, Session session)
			{
				String result = "";
				MenuState state = session.get(MenuState.PROP_USSD_MENU_STATE);
				if (stateProcessor != null)
					result = stateProcessor.menuExpandField(englishName, session, state.valueMap);
				if (result == null || result.isEmpty())
					result = state.valueMap.get(englishName);
				return result;
			}
		};

		// For Each Button
		for (UssdMenuButton button : menu.getButtons())
		{
			// Don't show disabled Buttons
			if (button.isDisabled())
				continue;
			
			// Check if Compulsory
			boolean compulsory = UssdMenuButton.TYPE_PREVIOUS.equals(button.getType()) && previousRequired //
					|| UssdMenuButton.TYPE_NEXT.equals(button.getType()) && nextRequired;

			// Get the Button Text
			Phrase phrase = button.getText();
			String text = Phrase.nullOrEmpty(phrase) ? null : phrase.safe(session.getLanguageID(), "");

			switch (button.getType())
			{
				case UssdMenuButton.TYPE_TEXT:
					if (stateProcessor != null)
					{
						List<Phrase> fields = new ArrayList<Phrase>();
						Phrase[] source = stateProcessor.menuCommandFields(em, session.getCompanyID());
						if (source != null)
						{
							for (Phrase field:source) fields.add(field);
						}
						source = stateProcessor.menuInformationFields(em, session.getCompanyID());
						if (source != null)
						{
							for (Phrase field:source) fields.add(field);
						}
						text = expander.expandNotification(phrase, session.getLocale(), fields.toArray(new Phrase[fields.size()]), session);
					}
					text = moniker.add(text, button);	
					break;

				case UssdMenuButton.TYPE_NAVIGATE:
					text = moniker.add(text, button);
					state.canProceed = true;
					break;

				case UssdMenuButton.TYPE_CAPTURE:
					text = moniker.add(text, button);
					state.canProceed = true;
					break;

				// Skip Command buttons which are not available
				case UssdMenuButton.TYPE_COMMAND:
					try
					{
						IMenuProcessor processor = getUssdProcessor(button.getCommandID());
						if (processor.menuMayExecute(em, session))
						{
							text = moniker.add(text, button);
							state.canProceed = true;
						}
						else
							text = null;
					}
					catch (RuleCheckException ex)
					{
						logger.info("rulecheck", ex);
            result.clear(); // Clear out existing text
						text = ex.getMessage();
					}
					catch (Throwable ex)
					{
						logger.info("Error processing command button", ex);
						text = null;
					}
					break;

				// For Result buttons, execute and display result
				case UssdMenuButton.TYPE_RESULT:
					Integer commandID = state.commandID;
					if (commandID == null || commandID < 0)
						text = null;
					else
					{
						state.commandID = -1;
						try
						{
							IMenuProcessor processor = getUssdProcessor(commandID);
							TransactionResponse response = processor.menuExecute(em, session, interaction, state.valueMap, state.options);
							text = response.wasSuccessful() ? response.getResponse() : //
									lookupError(em, session, response.getReturnCode());
						}
						catch (Throwable ex)
						{
							logger.info("Error processing result", ex);
							return null;
						}
					}
					break;

				case UssdMenuButton.TYPE_OPTION:
					text = moniker.add(text, button);
					state.canProceed = true;
					break;

				// For Auto Options, add Options
				case UssdMenuButton.TYPE_AUTO_OPTIONS:
					if (state.commandID != null && state.commandID > 0)
					{
						try
						{
							IMenuProcessor processor = getUssdProcessor(state.commandID);
							MenuOption[] options = processor.menuOptions(em, session, button.getCaptureField());
							if (options != null)
							{
								for (MenuOption option : options)
								{
									String optionText = text + option.getName().safe(session.getLanguageID(), "");
									UssdMenuButton optionButton = UssdMenuButton.createOption(state.commandID, Phrase.en(optionText), //
											option.getValue(), button.getCaptureField(), button.getNextMenuID());
									optionText = moniker.add(optionText, optionButton);
									skipLines = addLine(result, skipLines, maxLines, optionText, compulsory);
									state.canProceed = true;
								}
							}
						}
						catch (Throwable ex)
						{
							logger.info("Error processing auto options", ex);
							return null;
						}
					}
					text = null;
					break;

				case UssdMenuButton.TYPE_EXIT:
					text = moniker.add(text, button);
					state.canProceed = true;
					break;

				case UssdMenuButton.TYPE_PREVIOUS:
					if (previousRequired)
					{
						text = moniker.add(text, button);
						state.canProceed = true;
					}
					else
						text = null;
					break;

				case UssdMenuButton.TYPE_NEXT:
					if (nextRequired)
					{
						text = moniker.add(text, button);
						state.canProceed = true;
					}
					else
						text = null;
					break;
			}

			// Append Text to Menu
			skipLines = addLine(result, skipLines, maxLines, text, compulsory);

		}
		state.keyMap = moniker.keyMap;

		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	protected abstract IMenuProcessor getUssdProcessor(int id);

	private int addLine(List<String> lines, int skipLines, int maxLines, String text, boolean compulsory)
	{
		if (text == null)
			return skipLines;

		if (lines.size() >= maxLines && !compulsory)
			return skipLines;

		if (skipLines == 0 || compulsory)
			lines.add(text);
		else
			skipLines--;
		return skipLines;
	}

	private void reportError(String returnCode, IInteraction interaction, Session session)
	{
		// Lookup Error
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Get USSD Configuration
			String errorText = lookupError(em, session, returnCode);
			reply(session, interaction, errorText, false);
		}
		catch (Throwable ex)
		{
			logger.info("Error reporting error", ex);
		}
	}

	public String lookupError(EntityManager em, Session session, String returnCode)
	{
		CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
		TransactionsConfig config = companyInfo.getConfiguration(em, TransactionsConfig.class);
		String errorText = config.findErrorText(session.getLanguageID(), returnCode);
		return errorText;
	}

	public void reply(final Session session, IInteraction interaction, final String text, boolean isRequest)
	{
		interaction.setRequest(isRequest);
		INotificationText notificationText = new INotificationText()
		{

			@Override
			public String getText()
			{
				return text;
			}

			@Override
			public String getLanguageCode()
			{
				return session.getLocale().getISO3Language();
			}
		};

		interaction.reply(notificationText);
	}

	private class Moniker
	{
		Map<String, UssdMenuButton> keyMap = new HashMap<String, UssdMenuButton>();
		int option = 1;

		public String add(String text, UssdMenuButton button)
		{
			if (button != null && UssdMenuButton.TYPE_CAPTURE.equals(button.getType()))
			{
				keyMap.put(CAPTURE_KEY, button);
				return text;
			}

			Matcher matcher = keyPattern.matcher(text);
			if (matcher.find())
			{
				String key = matcher.group(1);
				while ("n".equals(key) || keyMap.containsKey(key))
				{
					key = Integer.toString(option++);
				}
				keyMap.put(key, button);
				text = text.substring(0, matcher.start()) + key + text.substring(matcher.end());
			}

			return text;
		}
	}
}
