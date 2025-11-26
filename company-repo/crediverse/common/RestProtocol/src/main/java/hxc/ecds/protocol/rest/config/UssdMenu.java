package hxc.ecds.protocol.rest.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class UssdMenu implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -2421813330124900770L;
	private static final int NAME_MAX_LENGTH = 256;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected List<UssdMenuButton> buttons = new ArrayList<UssdMenuButton>();
	protected String name;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public UssdMenu setId(int id)
	{
		this.id = id;
		return this;
	}

	public List<UssdMenuButton> getButtons()
	{
		return buttons;
	}

	public UssdMenu setButtons(List<UssdMenuButton> buttons)
	{
		this.buttons = buttons;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public UssdMenu setName(String name)
	{
		this.name = name;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n%s(%d):", name, id));

		if (buttons != null)
		{
			for (UssdMenuButton button : buttons)
			{
				sb.append('\n');
				sb.append(button.toString());
			}
		}

		return sb.toString();
	}

	public static Validator validate(Validator validator, List<UssdMenu> menus)
	{
		if (menus == null || menus.size() == 0)
			return validator;
		List<Integer> menuIDs = new ArrayList<Integer>();
		for (UssdMenu menu : menus)
		{
			if (menu != null)
			{
				if (menuIDs.contains(menu.id))
					validator.append(Violation.INVALID_VALUE, "id", null, "Duplicate menu id %d", menu.id);
				else
					menuIDs.add(menu.id);
			}
		}
		
		for (UssdMenu menu : menus)
		{
			validator.notNull("menu", menu);
			if (menu != null)
				menu.validate(validator, menu, menuIDs);
		}
		return validator;
	}

	private Validator validate(Validator validator, UssdMenu menu, List<Integer> menuIDs)
	{
		validator//
				.notEmpty("name", name, NAME_MAX_LENGTH) //
				.notNull("buttons", buttons);
		for (UssdMenuButton button : buttons)
		{
			validator.notNull("button", button);
			if (button != null)
				button.validate(validator, menuIDs);
		}
		return validator;
	}

}
