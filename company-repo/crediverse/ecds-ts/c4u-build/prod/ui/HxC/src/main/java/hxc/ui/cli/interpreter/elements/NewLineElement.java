package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;

public class NewLineElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^$";
	}

	@Override
	public boolean action(String action)
	{
		return true;
	}

}
