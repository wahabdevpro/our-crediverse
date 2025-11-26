package hxc.services.advancedtransfer;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.ValidationException;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasCommand.Processes;
import hxc.connectors.vas.VasCommandParser;

public class VASCommandTest
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testVasCommands()
	{
		assertTrue(compare("*{RecipientMSISDN}*{Amount}*{Pin}#{TransferModeID=oot}", "OOK{TransferModeID=OOK} {RecipientMSISDN} {Pin} {Amount}"));
		assertTrue(!compare("*{RecipientMSISDN}*{Amount}*{Pin}#{TransferModeID=oot}", "{TransferModeID} {RecipientMSISDN} {Pin} {Amount}"));
		assertTrue(!compare("*123#", "*123#"));
		assertTrue(compare("*123#", "*13#"));
		assertTrue(compare("*124*{Koos}#", "*123*{Koos}#"));
		assertTrue(!compare("*124*{Koos}#", "*124*7#"));
		assertTrue(compare("*123#", "*123#7#"));
	}

	private boolean compare(String string1, String string2)
	{
		VasCommand command1 = new VasCommand(Processes.suspend, string1);
		VasCommand command2 = new VasCommand(Processes.suspend, string2);

		VasCommandParser parser = new VasCommandParser(null, null, "Koos", "RecipientMSISDN", "TransferModeID", "Amount", "VariantID", "Pin", "TransferLimit")
		{
			@Override
			protected VasCommand[] getCommands()
			{
				return null;
			}

			@Override
			protected boolean parseCommandVariable(Processes process, String commandVariable, String value, CommandArguments arguments)
			{
				return false;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return null;
			}

			@Override
			protected ISoapConnector getSoapConnector()
			{
				return null;
			}

		};

		try
		{
			parser.validate(new VasCommand[] { command1, command2 });
		}
		catch (ValidationException e)
		{
			return false;
		}

		return true;
	}

	@Test
	public void testACTValidate() throws ValidationException
	{
		VasCommand command1 = new VasCommand(VasCommand.Processes.transfer, "*{RecipientMSISDN}*{Amount}*{Pin}#{TransferModeID=oot}");
		VasCommand command2 = new VasCommand(VasCommand.Processes.transfer, "OOK{TransferModeID=OOK} {RecipientMSISDN} {Pin} {Amount}");

		VasCommandParser parser = new VasCommandParser(null, null, "RecipientMSISDN", "TransferModeID", "Amount", "VariantID", "Pin", "TransferLimit")
		{

			@Override
			protected boolean parseCommandVariable(Processes process, String commandVariable, String value, CommandArguments arguments)
			{
				return false;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return null;
			}

			@Override
			protected ISoapConnector getSoapConnector()
			{
				return null;
			}

			@Override
			protected VasCommand[] getCommands()
			{
				return null;
			}
		};

		parser.validate(new VasCommand[] { command1, command2 });
	}

}
