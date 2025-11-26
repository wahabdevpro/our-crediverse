package hxc.utils.processmodel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;
import hxc.utils.processmodel.ui.UIProperties;

public class MenuItem extends Action implements IMenuItem
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ITexts text;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MenuItem(Action nextAction, String text)
	{
		super(null);
		this.text = new Texts(text);
		this.nextAction = nextAction;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@UIProperties(category = "Texts", editable = true)
	public ITexts getText()
	{
		return text;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public int addString(int number, IProcessState state, List<String> menuItems)
	{
		String caption = text.getSafeText(state.getLanguageID());

		if (caption == null || caption.length() == 0)
			return number;
		caption = state.getNotifications().get(state.getLanguageCode(), caption, state.getLocale(), state.getProperties());

		for (Pattern pattern : Menu.patterns)
		{
			Matcher matcher = pattern.matcher(caption);
			if (matcher.find())
			{
				String match = matcher.group(1);
				int startIndex = matcher.start(1);
				int endIndex = matcher.end(1);

				String index = match.substring(1);
				if (index.length() == 0)
					index = Integer.toString(number++);
				caption = caption.substring(0, startIndex) + index + caption.substring(endIndex);
				state.set(this, "index", index);
				break;
			}
		}
		menuItems.add(caption);

		return number;
	}

	@Override
	public Action nextActionFor(String input, IProcessState state)
	{
		String index = state.get(this, "index");
		if (input != null && input.equals(index))
			return nextAction;
		else
			return null;
	}
		
	@Override
	public Action getSingleActionFor(IProcessState state)
	{
		return null;
	}

	@Override
	public Action execute(IProcessState state, String command)
	{
		return nextAction;
	}

}
