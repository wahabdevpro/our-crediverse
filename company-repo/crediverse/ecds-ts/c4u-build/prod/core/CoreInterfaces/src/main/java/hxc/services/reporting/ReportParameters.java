package hxc.services.reporting;

import hxc.configuration.ValidationException;

public abstract class ReportParameters
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public abstract void validate() throws ValidationException;
}
