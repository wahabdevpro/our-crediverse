package hxc.utils.processmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.concurrent.hxc.Number;

import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;
import hxc.utils.processmodel.ui.UIProperties;
import hxc.utils.string.StringUtils;

public class Menu extends Action
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ITexts caption;
	private ITexts moreText = new Texts("More");
	private ITexts backText = new Texts("Back");
	private Action backAction = null;
	private List<IMenuItem> items = new ArrayList<IMenuItem>();
	private static final String NEWLINE = "\n";
	private static final Pattern p1 = Pattern.compile(".*(#\\d+|#\\*|#{2}).*");
	private static final Pattern p2 = Pattern.compile(".*(#{1}).*");
	public static final Pattern[] patterns = new Pattern[] { p1, p2 };
	private static final String MORE_INDEX = "moreIndex";
	private static final String BACK_INDEX = "backIndex";
	private static final String CURRENT_PAGE = "currPage";
	private IValueT<Long> numerator;
	private IValueT<Long> denominator;
	private boolean autoSelect = false;

	private Integer getCurrentPage(IProcessState state)
	{
		return state.get(Menu.this, CURRENT_PAGE);
	}

	private void setCurrentPage(IProcessState state, Integer currentPage)
	{
		state.set(Menu.this, CURRENT_PAGE, currentPage);
	}

	private String getMoreIndex(IProcessState state)
	{
		return state.get(Menu.this, MORE_INDEX);
	}

	private void setMoreIndex(IProcessState state, String moreIndex)
	{
		state.set(Menu.this, MORE_INDEX, moreIndex);
	}

	private String getBackIndex(IProcessState state)
	{
		return state.get(Menu.this, BACK_INDEX);
	}

	private void setBackIndex(IProcessState state, String backIndex)
	{
		state.set(Menu.this, BACK_INDEX, backIndex);
	}
	
	public boolean isAutoSelect()
	{
		return autoSelect;
	}

	public void setAutoSelect(boolean autoSelect)
	{
		this.autoSelect = autoSelect;
	}




	private IValueT<String> inputText = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(Menu.this, "inputText");
		}
	};

	private IValueT<Number> inputNumber = new IValueT<Number>()
	{
		@Override
		public Number getValue(IProcessState state)
		{
			return new Number(inputText.getValue(state));
		}
	};

	private IValueT<Integer> inputAmount = new IValueT<Integer>()
	{
		@Override
		public Integer getValue(IProcessState state)
		{
			try
			{
				Long result = parseInputNumber(state);
				return result == null ? null : (int) (long) result;
			}
			catch (NumberFormatException ex)
			{
				return Integer.MAX_VALUE;
			}

		}
	};

	private IValueT<Long> inputTotal = new IValueT<Long>()
	{
		@Override
		public Long getValue(IProcessState state)
		{
			try
			{
				return parseInputNumber(state);
			}
			catch (NumberFormatException ex)
			{
				return Long.MAX_VALUE;
			}
		}

	};

	private Long parseInputNumber(IProcessState state)
	{
		if (numerator != null && denominator != null)
		{
			Long num = numerator.getValue(state);
			Long denum = denominator.getValue(state);
			if (num != null && denum != null)
			{
				return StringUtils.parseScaled(inputText.getValue(state), num, denum);
			}

		}

		return Long.parseLong(inputText.getValue(state));
	}

	public IValueT<Long> getNumerator()
	{
		return numerator;
	}

	public void setNumerator(IValueT<Long> numerator)
	{
		this.numerator = numerator;
	}

	public IValueT<Long> getDenominator()
	{
		return denominator;
	}

	public void setDenominator(IValueT<Long> denominator)
	{
		this.denominator = denominator;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@UIProperties(category = "Texts", editable = true)
	public ITexts getCaption()
	{
		return caption;
	}

	public void setCaption(ITexts caption)
	{
		this.caption = caption;
	}

	@UIProperties(category = "Texts", editable = true)
	public ITexts getMoreText()
	{
		return moreText;
	}

	public void setMoreText(ITexts moreText)
	{
		this.moreText = moreText;
	}

	@UIProperties(category = "Menu", value = "...")
	public List<IMenuItem> getItems()
	{
		return items;
	}

	@UIProperties(category = "Input", value = "input")
	public IValueT<String> getInputText()
	{
		return inputText;
	}

	private void setInputText(IProcessState state, String inputText)
	{
		state.set(Menu.this, "inputText", inputText);
	}

	@UIProperties(category = "Input", value = "number")
	public IValueT<Number> getInputNumber()
	{
		return inputNumber;
	}

	@UIProperties(category = "Input", value = "amount")
	public IValueT<Integer> getInputAmount()
	{
		return inputAmount;
	}

	public IValueT<Long> getInputTotal()
	{
		return inputTotal;
	}

	public ITexts getBackText()
	{
		return backText;
	}

	public Action getBackAction()
	{
		return backAction;
	}

	// public void setStringValueConverter(IValueT<IStringValue> stringValueConverter)
	// {
	// this.stringValueConverterx = stringValueConverter;
	// }

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Menu(Action afterAction, String caption)
	{
		super(afterAction);
		this.caption = new Texts(caption);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public MenuItem addItem(String caption)
	{
		MenuItem item = new MenuItem(null, caption);
		items.add(item);
		return item;
	}

	public MenuItem addItem(Action nextAction, String caption)
	{
		MenuItem item = new MenuItem(nextAction, caption);
		items.add(item);
		return item;
	}

	public void setBackItem(Action backAction, String modelText, String... texts)
	{
		this.backAction = backAction;
		backText = new Texts(modelText, texts);
	}

	public <T> MenuItems<T> addItems(String caption, String emptyCaption, IValueT<T[]> args)
	{
		MenuItems<T> item = new MenuItems<T>(caption, emptyCaption, args);
		items.add(item);
		return item;
	}

	@Override
	public Action execute(IProcessState state, String command)
	{
		// Get the Current Page
		Integer currentPage = getCurrentPage(state);
		if (currentPage == null)
		{
			currentPage = 0;
			setCurrentPage(state, 0);
		}

		String output = state.getOutput();
		if (output != null && output.length() > 0)
		{
			String input = command;
			setInputText(state, input);

			if (input != null)
			{
				if (input.equals(getMoreIndex(state)))
				{
					setCurrentPage(state, ++currentPage);
					state.setOutput(null);
					return this;
				}
				else if (input.equals(getBackIndex(state)))
				{ 
					if (currentPage > 0)
					{
						setCurrentPage(state, --currentPage);
						state.setOutput(null);
						return this;
					}
					else
					{
						state.setOutput(null);
						return backAction;
					}
				}
			}

			for (IMenuItem item : items)
			{
				Action nextAction = item.nextActionFor(input, state);
				if (nextAction != null)
				{
					state.setOutput(null);
					return nextAction;
				}
			}

			if (nextAction != null)
			{
				state.setOutput(null);
				return nextAction;
			}

		}
		else
		{

			// Get the Caption Text
			String title = caption.getSafeText(state.getLanguageID());
			title = state.getNotifications().get(state.getLanguageCode(), title, state.getLocale(), state.getProperties());
			int titleLength = 0;
			if (title != null && title.length() > 0)
			{
				title = state.getNotifications().get(state.getLanguageCode(), title, state.getLocale(), state.getProperties());
				titleLength = title.length() + 1;
			}
			
			// Auto Select
			if (autoSelect && items.size() == 1)
			{		
				IMenuItem menuItem = (IMenuItem)items.get(0);
				Action nextAction = menuItem.getSingleActionFor(state);
				if (nextAction != null)
			       return nextAction;
			}

			// Get the normal Menu Texts
			List<String> menuItems = new ArrayList<String>();
			int index = 1;
			for (IMenuItem item : items)
			{
				index = item.addString(index, state, menuItems);
			}

			// Get the More Menu Text
			int moreLength = 0;
			SpecialItem moreItem = getSpecialItem(index, moreText, state);
			if (moreItem != null)
			{
				index = moreItem.newIndex;
				moreLength = moreItem.text.length() + 1;
			}

			// Get the Back Menu Text
			int backLength = 0;
			SpecialItem backItem = getSpecialItem(index, backText, state);
			if (backItem != null)
			{
				index = backItem.newIndex;
				backLength = backItem.text.length() + 1;
			}

			// Cut into Pages
			int pageNo = -1;
			int nextItemToAdd = 0;
			while (true)
			{
				pageNo++;

				int charsAvailable = (moreLength > 0 ? state.getMaxOutputLength() : Integer.MAX_VALUE) - titleLength;
				boolean mustAddBack = backLength > 0 && (pageNo > 0 || backItem != null);
				if (mustAddBack)
					charsAvailable -= backLength;

				// Add Page Items
				Stack<String> pageItems = new Stack<String>();
				while (nextItemToAdd < menuItems.size())
				{
					String item = menuItems.get(nextItemToAdd++);
					pageItems.push(item);
					charsAvailable -= item.length() + 1;
					if (charsAvailable < 0)
						break;
				}

				// Backtrack if too long
				boolean mustAddMore = charsAvailable < 0;
				if (mustAddMore)
				{
					while (charsAvailable < moreLength && !pageItems.isEmpty())
					{
						String item = pageItems.pop();
						charsAvailable += item.length() + 1;
						nextItemToAdd--;
					}
				}

				// Create Text if this is the Current Page
				if (pageNo == currentPage || nextItemToAdd >= menuItems.size())
				{
					setCurrentPage(state, pageNo);

					// Append Title
					StringBuilder sb = new StringBuilder();
					if (titleLength > 0)
					{
						sb.append(title);
						sb.append(NEWLINE);
					}

					// Append Items
					for (String item : pageItems)
					{
						sb.append(item);
						sb.append(NEWLINE);
					}

					// Append More
					if (mustAddMore)
					{
						sb.append(moreItem.text);
						sb.append(NEWLINE);
						setMoreIndex(state, moreItem.index);
					}
					else
						setMoreIndex(state, (String) null);

					// Append Back
					if (mustAddBack)
					{
						sb.append(backItem.text);
						sb.append(NEWLINE);
						setBackIndex(state, backItem.index);
					}
					else
						setBackIndex(state, (String) null);

					state.setOutput(sb.toString());
					break;

				}

			}

		}

		return null;

	}

	private SpecialItem getSpecialItem(int oldIndex, ITexts texts, IProcessState state)
	{
		String text = texts.getSafeText(state.getLanguageID());
		if (text == null || text.length() == 0)
			return null;

		text = state.getNotifications().get(state.getLanguageCode(), text, state.getLocale(), state.getProperties());

		for (Pattern pattern : Menu.patterns)
		{
			Matcher matcher = pattern.matcher(text);
			if (matcher.find())
			{
				String match = matcher.group(1);
				int startIndex = matcher.start(1);
				int endIndex = matcher.end(1);

				String index = match.substring(1);
				if (index.length() == 0)
					index = Integer.toString(oldIndex++);
				SpecialItem result = new SpecialItem();
				result.text = text.substring(0, startIndex) + index + text.substring(endIndex);
				result.index = index;
				result.newIndex = oldIndex;
				return result;
			}
		}

		return null;
	}

	private class SpecialItem
	{
		String text;
		String index;
		int newIndex;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

}
