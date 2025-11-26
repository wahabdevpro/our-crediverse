package hxc.services.credittransfer;

import hxc.processmodel.IProcess;
import hxc.utils.processmodel.End;
import hxc.utils.processmodel.ErrorDisplay;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.SubscriptionTest;
import hxc.utils.processmodel.UssdStart;

public class CreditTransferMenu
{
	@SuppressWarnings("unused")
	public static IProcess getMenuProcess(String serviceID)
	{
		// Start with Short Code 143
		UssdStart start = new UssdStart(serviceID, "UssdMenu");

		// Error exit
		ErrorDisplay errorDisplayEnd = new ErrorDisplay(null, start.getServiceID());

		// Normal Exit
		End end = new End(null, "Thank you for using the Credit Transfer Service");

		// Make Test
		SubscriptionTest subscribeTest = new SubscriptionTest(start, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), null);

		// Root
		Menu rootMenu = new Menu(subscribeTest, "Credit Transfer Service");

		return start;
	}
}
