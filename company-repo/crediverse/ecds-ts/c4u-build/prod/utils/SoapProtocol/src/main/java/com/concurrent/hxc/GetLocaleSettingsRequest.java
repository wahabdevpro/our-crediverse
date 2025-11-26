package com.concurrent.hxc;

import hxc.services.notification.IPhrase;

public class GetLocaleSettingsRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetLocaleSettingsRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetLocaleSettingsRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetLocaleSettingsRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.getLanguageID() == null || request.getLanguageID() < 1 || request.getLanguageID() > IPhrase.MAX_LANGUAGES)
			return "Bad LanguageID";

		return null;
	}

}
