package hxc.ui.cli.interpreter.elements.parent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import hxc.ui.cli.ICLIClient;
import hxc.ui.cli.connector.ICLIConnector;
import hxc.ui.cli.interpreter.elements.BackElement;
import hxc.ui.cli.interpreter.elements.ExitElement;
import hxc.ui.cli.interpreter.elements.ExportElement;
import hxc.ui.cli.interpreter.elements.FitnessElement;
import hxc.ui.cli.interpreter.elements.HelpElement;
import hxc.ui.cli.interpreter.elements.ImportElement;
import hxc.ui.cli.interpreter.elements.ListElement;
import hxc.ui.cli.interpreter.elements.LogoutElement;
import hxc.ui.cli.interpreter.elements.NewLineElement;
import hxc.ui.cli.interpreter.elements.PathElement;
import hxc.ui.cli.interpreter.elements.ResilientElement;
import hxc.ui.cli.interpreter.elements.RevertServiceElement;
import hxc.ui.cli.interpreter.elements.SMSElement;
import hxc.ui.cli.interpreter.elements.SearchElement;
import hxc.ui.cli.interpreter.elements.USSDElement;
import hxc.ui.cli.interpreter.elements.VersionElement;
import hxc.ui.cli.system.ICLISystem;

public abstract class Element implements IElement
{

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Properties
		//
		// /////////////////////////////////
	
	protected ICLIConnector connector;
	protected ICLISystem system;
	protected ICLIClient client;

	public String[] names = { "Element" };
	public String description = "Superclass for other elements.";
	public boolean function;
	public String error = "Command or path not found. Please use ls or help for more information.";

	// @formatter:off
	protected static Element[] elements = { new BackElement(), new ExitElement(), new FitnessElement(), new HelpElement(), new ListElement(), new LogoutElement(), new NewLineElement(),
			new ResilientElement(), new RevertServiceElement(), new SearchElement(), new SMSElement(), new USSDElement(), new VersionElement(), 
			new ExportElement(), new ImportElement(), 
			// Final Resort
			new PathElement() };

	// @formatter:on

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		// /////////////////////////////////
	
	public Element()
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Implementation
		//
		// /////////////////////////////////
	
	@Override
	public void setConnector(ICLIConnector connector)
	{
		this.connector = connector;
	}

	@Override
	public void setSystem(ICLISystem system)
	{
		this.system = system;
	}

	@Override
	public void setClient(ICLIClient client)
	{
		this.client = client;
	}

	// Checks whether the regex matches the element
	@Override
	public boolean matches(String comparison)
	{
		try
		{
			return Pattern.matches(regex(), comparison.toLowerCase());
		}
		catch (PatternSyntaxException exc)
		{
			return false;
		}

	}

	@Override
	public abstract String regex();

	@Override
	public abstract boolean action(String action);

	@Override
	public String getError()
	{
		return error;
	}

	// Gets the element from the array of elements based on the regex
	public static Element generateElement(String element)
	{
		// Iterate through the elements
		for (int i = 0; i < elements.length; i++)
		{
			// Get the element
			IElement e = elements[i];
			
			// Check if the element matches the regex
			if (e.matches((element != null) ? element : "exit"))
			{
				// Return the element
				return elements[i];
			}
		}
		return null;
	}

	// Gets all the current elements
	public static Element[] getAllElements()
	{
		return Element.elements;
	}

	// Sets the elements
	protected static void setElements(Element[] elements)
	{
		Element.elements = elements;
	}

}
