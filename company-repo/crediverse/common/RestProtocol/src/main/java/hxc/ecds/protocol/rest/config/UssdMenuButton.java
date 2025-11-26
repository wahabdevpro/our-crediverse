package hxc.ecds.protocol.rest.config;

import java.io.Serializable;
import java.util.List;

import hxc.ecds.protocol.rest.UssdCommand;
import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class UssdMenuButton implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String TYPE_TEXT = "T";
	public static final String TYPE_NAVIGATE = "N";
	public static final String TYPE_CAPTURE = "C";
	public static final String TYPE_COMMAND = "P";
	public static final String TYPE_RESULT = "E";
	public static final String TYPE_OPTION = "O";
	public static final String TYPE_AUTO_OPTIONS = "A";
	public static final String TYPE_EXIT = "X";
	public static final String TYPE_PREVIOUS = "<";
	public static final String TYPE_NEXT = ">";

	private static final long serialVersionUID = -1504869026451357204L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String type = TYPE_TEXT;
	protected Phrase text;
	protected String captureField;
	protected Integer commandID;
	protected Integer nextMenuID;
	protected String value;
	protected boolean disabled = false;
	protected int options = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getType()
	{
		return type;
	}

	public UssdMenuButton setType(String type)
	{
		this.type = type;
		return this;
	}

	public Phrase getText()
	{
		return text;
	}

	public UssdMenuButton setText(Phrase text)
	{
		this.text = text;
		return this;
	}

	public String getCaptureField()
	{
		return captureField;
	}

	public UssdMenuButton setCaptureField(String captureField)
	{
		this.captureField = captureField;
		return this;
	}

	public Integer getCommandID()
	{
		return commandID;
	}

	public UssdMenuButton setCommandID(Integer commandID)
	{
		this.commandID = commandID;
		return this;
	}

	public Integer getNextMenuID()
	{
		return nextMenuID;
	}

	public UssdMenuButton setNextMenuID(Integer nextMenuID)
	{
		this.nextMenuID = nextMenuID;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public UssdMenuButton setValue(String value)
	{
		this.value = value;
		return this;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public UssdMenuButton setDisabled(boolean disabled)
	{
		this.disabled = disabled;
		return this;
	}

	public int getOptions()
	{
		return options;
	}

	public UssdMenuButton setOptions(int options)
	{
		this.options = options;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public UssdMenuButton()
	{

	}

	public static UssdMenuButton createText(Phrase text)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_TEXT) //
				.setText(text);
	}

	public static UssdMenuButton createNavigate(Phrase text, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_NAVIGATE) //
				.setText(text) //
				.setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createExit(Phrase text, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_NAVIGATE) //
				.setText(text).setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createCommand(UssdCommand command, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_COMMAND) //
				.setText(command.getName().format("[n]) %s")) //
				.setCommandID(command.getId()) //
				.setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createCommand(int commandID, Phrase text, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_COMMAND) //
				.setText(text) //
				.setCommandID(commandID) //
				.setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createCapture(int commandID, Phrase text, String fieldName, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_CAPTURE) //
				.setText(text) //
				.setCaptureField(fieldName) //
				.setCommandID(commandID) //
				.setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createResult(int commandID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_RESULT).setCommandID(commandID);
	}

	public static UssdMenuButton createOption(int commandID, Phrase text, String value, String fieldName, Integer nextMenuID)
	{
		return new UssdMenuButton() //
				.setText(text) //
				.setType(UssdMenuButton.TYPE_OPTION) //
				.setCaptureField(fieldName) //
				.setNextMenuID(nextMenuID) //
				.setCommandID(commandID) //
				.setValue(value);
	}

	public static UssdMenuButton createOptions(int commandID, Phrase text, String fieldName, int nextMenuID)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_AUTO_OPTIONS) //
				.setText(text) //
				.setCaptureField(fieldName) //
				.setCommandID(commandID) //
				.setNextMenuID(nextMenuID);
	}

	public static UssdMenuButton createNext(Phrase text)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_NEXT) //
				.setText(text);
	}

	public static UssdMenuButton createPrevious(Phrase text)
	{
		return new UssdMenuButton() //
				.setType(UssdMenuButton.TYPE_PREVIOUS) //
				.setText(text);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		switch (type)
		{
			case TYPE_TEXT:
				return String.format("%s", safe(text));
			case TYPE_NAVIGATE:
				return String.format("%s (->Menu%d)", safe(text), nextMenuID);
			case TYPE_COMMAND:
				return String.format("%s (Command %d)", safe(text), commandID);
			case TYPE_RESULT:
				return String.format("(Execution Result)");
			case TYPE_EXIT:
				return String.format("%s (Exit)", safe(text));
			case TYPE_PREVIOUS:
				return String.format("%s (Previous)", safe(text));
			case TYPE_NEXT:
				return String.format("%s (Next)", safe(text));

			case TYPE_CAPTURE:
				return String.format("%s (=>%s ->Menu%d)", safe(text), captureField, nextMenuID);
			case TYPE_OPTION:
				return String.format("%s (%s=>%s ->Menu%d)", safe(text), value, captureField, nextMenuID);
			case TYPE_AUTO_OPTIONS:
				return String.format("Option List (=>%s ->Menu%d)", captureField, nextMenuID);

			default:
				return "??";
		}

	}

	private static String safe(Phrase text)
	{
		return text == null ? "??" : text.safe(Phrase.ENG, "??");
	}

	public Validator validate(Validator validator, List<Integer> menuIDs)
	{
		validator //
				.oneOf("type", type, TYPE_TEXT, TYPE_NAVIGATE, TYPE_CAPTURE, TYPE_COMMAND, TYPE_RESULT, TYPE_OPTION, //
						TYPE_AUTO_OPTIONS, TYPE_EXIT, TYPE_PREVIOUS, TYPE_NEXT);
		
		if (nextMenuID != null && !menuIDs.contains(nextMenuID))
			validator.append(Violation.INVALID_VALUE, "nextMenuID", null, "Invalid menu id %d", nextMenuID);
		
		switch (type)
		{
			case TYPE_TEXT:
				return validator//
						.notEmpty("text", text);
								
			case TYPE_NAVIGATE:
				return validator//
						.notEmpty("text", text) //
						.notNull("nextMenuID", nextMenuID);		
				
			case TYPE_CAPTURE:
				return validator//
						.notEmpty("text", text) //
						.notNull("captureField", captureField) //
						.notNull("commandID", commandID) //
						.notNull("nextMenuID", nextMenuID);
				
			case TYPE_COMMAND:
				return validator//
						.notEmpty("text", text) //
						.notNull("commandID", commandID) //
						.notNull("nextMenuID", nextMenuID);
				
			case TYPE_RESULT:
				return validator//
						.notNull("commandID", commandID); 
				
			case TYPE_OPTION:
				return validator//
						.notEmpty("text", text) //
						.notNull("captureField", captureField) //
						.notNull("commandID", commandID) //
						.notNull("value", value) //
						.notNull("nextMenuID", nextMenuID);						
				
			case TYPE_AUTO_OPTIONS:
				return validator//
						.notEmpty("text", text) //
						.notNull("captureField", captureField) //
						.notNull("commandID", commandID) //
						.notNull("nextMenuID", nextMenuID);	
			
			case TYPE_EXIT:
				return validator//
						.notNull("nextMenuID", nextMenuID);	
				
			case TYPE_PREVIOUS:
				return validator//
						.notEmpty("text", text);
				
			case TYPE_NEXT:
				return validator//
						.notEmpty("text", text);
		}

		return validator;
	}
}
