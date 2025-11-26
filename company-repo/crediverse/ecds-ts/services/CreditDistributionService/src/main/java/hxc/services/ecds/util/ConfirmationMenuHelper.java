package hxc.services.ecds.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.config.IConfirmationMenuConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuConstructor;
import hxc.utils.calendar.DateTime;

public class ConfirmationMenuHelper
{

	public static void constructMenus(IMenuProcessor menuProcessor, IConfirmationMenuConfig config, int commandID, String confirmPhrase, String cancelPhrase, String confirmDuplicatePhrase)
	{

		if (config.getConfirmationMenus() == null || config.getConfirmationMenus().size() == 0)
		{
			List<UssdMenu> confirmationMenus = new MenuConstructor()
			{
				@Override
				protected IMenuProcessor getUssdProcessor(int id)
				{
					return menuProcessor;
				}
			}.createConfirmationMenu(commandID, confirmPhrase, cancelPhrase);
			config.setConfirmationMenus(confirmationMenus);
		}

		if (config.getDeDuplicationMenus() == null || config.getDeDuplicationMenus().size() == 0)
		{
			List<UssdMenu> deDuplicationMenus = new MenuConstructor()
			{
				@Override
				protected IMenuProcessor getUssdProcessor(int id)
				{
					return menuProcessor;
				}
			}.createDeDuplicationMenu(commandID, confirmDuplicatePhrase, cancelPhrase);
			config.setDeDuplicationMenus(deDuplicationMenus);
		}
	}

	public static List<UssdMenu> triggerConfirmation(IMenuProcessor menuProcessor, EntityManager em, IInteraction interaction, //
			ICreditDistribution context, IConfirmationMenuConfig config, Session session, int commandID, String type, //
			String recipientMSISDN, BigDecimal amount, Integer bundleID, Map<String, String> values)
	{
		List<UssdMenu> confirmationMenu = null;
		boolean isUssdAgent = interaction.getChannel().equals(Channels.USSD) && session.getAgent() != null;
		if (isUssdAgent)
		{
			if (config.isEnableDeDuplication())
			{
				int maxDuplicateCheckMinutes = config.getMaxDuplicateCheckMinutes();
				if (maxDuplicateCheckMinutes > 0)
				{
					DateTime now = DateTime.getNow();
					DateTime since = now.addMinutes(-maxDuplicateCheckMinutes);
					recipientMSISDN = context.toMSISDN(recipientMSISDN);
					Transaction duplicate = bundleID == null ? Transaction.findDuplicateForAgent(em, type, session.getAgentID(), //
							recipientMSISDN, amount, since, session.getCompanyID()) //
							: Transaction.findDuplicateBundleForAgent(em, type, session.getAgentID(), //
									recipientMSISDN, bundleID, since, session.getCompanyID());
	
					if (duplicate != null)
					{
						long diffMillis = now.getTime() - duplicate.getStartTime().getTime();
						long minutes = (diffMillis + 59999) / 60000;
						values.put(IConfirmationMenuConfig.MINS_SINCE_LAST, Long.toString(minutes));
						confirmationMenu = config.getDeDuplicationMenus();
					}
				}
			}

			if (confirmationMenu == null && session.getAgent().isConfirmUssd()) {
				confirmationMenu = config.getConfirmationMenus();
			}
		}
		return confirmationMenu;
	}

}
