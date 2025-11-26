package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;

public class BackElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "back", ".." };
		description = "Returns to the previous item.";
		function = true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^back$|^[.][.]$";
	}

	@Override
	public boolean action(String action)
	{
		// Go back in the system
		return system.back();
	}

}
