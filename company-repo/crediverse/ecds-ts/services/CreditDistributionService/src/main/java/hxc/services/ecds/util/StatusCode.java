package hxc.services.ecds.util;

import javax.ws.rs.core.Response.Status;

public class StatusCode
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final StatusCode CANNOT_BE_EMPTY = new StatusCode("CANNOT_BE_EMPTY", Status.NOT_ACCEPTABLE);
	public static final StatusCode RECURSIVE = new StatusCode("RECURSIVE", Status.NOT_ACCEPTABLE);
	public static final StatusCode TOO_LONG = new StatusCode("TOO_LONG", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_VALUE = new StatusCode("INVALID_VALUE", Status.NOT_ACCEPTABLE);
	public static final StatusCode DUPLICATE_VALUE = new StatusCode("DUPLICATE_VALUE", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANNOT_HAVE_VALUE = new StatusCode("CANNOT_HAVE_VALUE", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANT_BE_CHANGED = new StatusCode("CANT_BE_CHANGED", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANT_SET_AS_PERMANENT = new StatusCode("CANT_SET_AS_PERMANENT", Status.FORBIDDEN);
	public static final StatusCode NOT_SAME = new StatusCode("NOT_SAME", Status.NOT_ACCEPTABLE);
	public static final StatusCode TOO_SMALL = new StatusCode("TOO_SMALL", Status.NOT_ACCEPTABLE);
	public static final StatusCode TOO_LARGE = new StatusCode("TOO_LARGE", Status.NOT_ACCEPTABLE);
	public static final StatusCode FAILED_TO_SAVE = new StatusCode("FAILED_TO_SAVE", Status.NOT_ACCEPTABLE);
	public static final StatusCode FAILED_TO_DELETE = new StatusCode("FAILED_TO_DELETE", Status.NOT_ACCEPTABLE);
	public static final StatusCode NOT_FOUND = new StatusCode("NOT_FOUND", Status.NOT_FOUND);
	public static final StatusCode FORBIDDEN = new StatusCode("FORBIDDEN", Status.FORBIDDEN);
	public static final StatusCode AMBIGUOUS = new StatusCode("AMBIGUOUS", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANNOT_ADD = new StatusCode("CANNOT_ADD", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANNOT_DELETE = new StatusCode("CANNOT_DELETE", Status.NOT_ACCEPTABLE);
	public static final StatusCode RESOURCE_IN_USE = new StatusCode("RESOURCE_IN_USE", Status.NOT_ACCEPTABLE);
	public static final StatusCode NON_ZERO_BALANCE = new StatusCode("NON_ZERO_BALANCE", Status.NOT_ACCEPTABLE);
	public static final StatusCode CANNOT_DELETE_SELF = new StatusCode("CANNOT_DELETE_SELF", Status.NOT_ACCEPTABLE);
	public static final StatusCode UNAUTHORIZED = new StatusCode("UNAUTHORIZED", Status.UNAUTHORIZED);
	public static final StatusCode TAMPERED = new StatusCode("TAMPERED", Status.CONFLICT);
	public static final StatusCode TECHNICAL_PROBLEM = new StatusCode("TECHNICAL_PROBLEM", Status.INTERNAL_SERVER_ERROR);
	public static final StatusCode DAY_COUNT_LIMIT = new StatusCode("DAY_COUNT_LIMIT", Status.NOT_ACCEPTABLE);
	public static final StatusCode DAY_AMOUNT_LIMIT = new StatusCode("DAY_AMOUNT_LIMIT", Status.NOT_ACCEPTABLE);
	public static final StatusCode MONTH_COUNT_LIMIT = new StatusCode("MONTH_COUNT_LIMIT", Status.NOT_ACCEPTABLE);
	public static final StatusCode MONTH_AMOUNT_LIMIT = new StatusCode("MONTH_AMOUNT_LIMIT", Status.NOT_ACCEPTABLE);
	public static final StatusCode INSUFFICIENT_FUNDS = new StatusCode("INSUFFICIENT_FUNDS", Status.NOT_ACCEPTABLE);
	public static final StatusCode LIMIT_REACHED = new StatusCode("LIMIT_REACHED", Status.NOT_ACCEPTABLE);

	public static final StatusCode ALREADY_REGISTERED = new StatusCode("ALREADY_REGISTERED", Status.FORBIDDEN);
	public static final StatusCode NOT_REGISTERED = new StatusCode("NOT_REGISTERED", Status.FORBIDDEN);
	public static final StatusCode INVALID_PIN = new StatusCode("INVALID_PIN", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_CHANNEL = new StatusCode("INVALID_CHANNEL", Status.FORBIDDEN);
	public static final StatusCode INVALID_STATE = new StatusCode("INVALID_STATE", Status.FORBIDDEN);
	public static final StatusCode INVALID_TO_ROOT = new StatusCode("INVALID_TO_ROOT", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_FROM_SUBSCRIBER = new StatusCode("INVALID_FROM_SUBSCRIBER", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_TIER_TO_STORE = new StatusCode("INVALID_TIER_TO_STORE", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_TIER_TO_WHOLESALER = new StatusCode("INVALID_TIER_TO_WHOLESALER", Status.NOT_ACCEPTABLE);
	public static final StatusCode INVALID_TIER_TO_SUBSCRIBER = new StatusCode("INVALID_TIER_TO_SUBSCRIBER", Status.NOT_ACCEPTABLE);
  public static final StatusCode ALREADY_DEACTIVATED = new StatusCode("ALREADY_DEACTIVATED", Status.PRECONDITION_FAILED);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String name;
	private Status status;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getName()
	{
		return name;
	}

	public Status getStatus()
	{
		return status;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public StatusCode(String name, Status status)
	{
		this.name = name;
		this.status = status;
	}
}
