package hxc.utils.processmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;
import hxc.utils.processmodel.ui.UIProperties;

public class MenuItems<T> extends Action implements IMenuItem
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ITexts text;
	private ITexts emptyText;
	private IValueT<T[]> values;

	private IValueT<T> selectedValue = new IValueT<T>()
	{
		@Override
		public T getValue(IProcessState state)
		{
			return state.get(MenuItems.this, "selectedValue");
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@UIProperties(category = "Output", value = "value")
	public IValueT<T> getSelectedValue()
	{
		return selectedValue;
	}

	@UIProperties(category = "Texts", editable = true)
	public ITexts getText()
	{
		return text;
	}

	public void setText(ITexts text)
	{
		this.text = text;
	}

	@UIProperties(category = "Texts", editable = true)
	public ITexts getEmptyText()
	{
		return emptyText;
	}

	public void setEmptyText(ITexts emptyText)
	{
		this.emptyText = emptyText;
	}

	@UIProperties(category = "Input", value = "values")
	public IValueT<T[]> getValues()
	{
		return values;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MenuItems(String text, String emptyText, IValueT<T[]> values)
	{
		super(null);
		this.text = new Texts(text);
		this.values = values;
		this.emptyText = new Texts(emptyText);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public int addString(int number, IProcessState state, List<String> menuItems)
	{
		Map<String, T> choices = new HashMap<String, T>();
		state.set(this, "choices", choices);
		T[] valueArray = values != null ? values.getValue(state) : null;

		// If there are no values
		if (values == null || valueArray == null || valueArray.length == 0)
		{
			String caption = emptyText.getSafeText(state.getLanguageID());
			if (caption != null && caption.length() > 0)
				menuItems.add(caption);
			return number;
		}

		// Add Values
		for (T value : valueArray)
		{
			String caption = text.getSafeText(state.getLanguageID());
			if (caption == null || caption.length() == 0)
				return number;
			caption = String.format(caption.replace("{}", "%s"), value);

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
					choices.put(index, value);
					break;
				}
			}
			menuItems.add(caption);
		}

		return number;

	}

	@Override
	public Action nextActionFor(String input, IProcessState state)
	{
		Map<String, T> choices = state.get(this, "choices");
		if (choices.containsKey(input))
		{
			T selectedValue = choices.get(input);
			state.set(this, "selectedValue", selectedValue);
			return nextAction;
		}
		else
			return null;
	}
	
	

	@Override
	public Action getSingleActionFor(IProcessState state)
	{
		T[] valueArray = values != null ? values.getValue(state) : null;
		if (valueArray != null && valueArray.length == 1)
		{
			state.set(this, "selectedValue", valueArray[0]);
			return nextAction;
		}
		
    	return null;
	}

	@Override
	public Action execute(IProcessState state, String command)
	{
		return nextAction;
	}

}
