package hxc.utils.processmodel;

import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;

public class End extends Action
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ITexts message;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public ITexts getMessage()
	{
		return message;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public End(Action afterAction, String message)
	{
		super(afterAction);
		this.message = new Texts(message);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public Action execute(IProcessState state, String command)
	{
		// Format and set the Output Text
		String text = message.getSafeText(state.getLanguageID());
		text = state.getNotifications().get(state.getLanguageCode(), text, state.getLocale(), state.getProperties());
		state.setOutput(text);

		// Mark as Completed
		state.setCompleted(true);
		return null;
	}

}
