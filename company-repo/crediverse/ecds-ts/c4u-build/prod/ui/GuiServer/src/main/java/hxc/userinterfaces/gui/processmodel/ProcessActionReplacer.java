package hxc.userinterfaces.gui.processmodel;

import java.util.ArrayList;
import java.util.List;

import hxc.processmodel.IProcess;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.End;
import hxc.utils.processmodel.ErrorDisplay;
import hxc.utils.processmodel.IMenuItem;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.Start;
import hxc.utils.processmodel.Test;

public class ProcessActionReplacer
{
	private List<Action> bagage = null;
	private boolean DEBUG = false;

	public boolean findAndReplaceAction(IProcess process, Action action, int hashCode)
	{
		Action start = (Action) process.getStart();
		bagage = new ArrayList<Action>();
		boolean result = findAndReplace(start, action, hashCode, 0);
		return result;
	}

	private void sout(Action act, int level)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < level; i++)
		{
			sb.append("  ");
		}

		if (act instanceof Menu)
		{
			sb.append(((Menu) act).getCaption().getText(0));
		}
		else if (act instanceof MenuItem)
		{
			sb.append(((MenuItem) act).getText().getModelText());
		}
		else if (act instanceof MenuItems)
		{
			sb.append(((MenuItems) act).getText().getModelText());
		}
		else if (act instanceof Start)
		{
			sb.append("Start");
		}
		else if (act instanceof ErrorDisplay)
		{
			sb.append("Error");
		}
		else if (act instanceof Test)
		{
			sb.append("Test");
		}
		else
		{
			sb.append("Unknown");
		}
	}

	private boolean findAndReplace(Action current, Action replaceWith, int hashCode, int level)
	{
		boolean updated = false;

		if (DEBUG && (current != null))
		{
			sout(current, level);
		}
		try
		{
			// End of the line
			if (current == null)
			{
				return false; // End Of Line
			}
			else if (current.hashCode() == hashCode)
			{
				if (current instanceof Menu)
				{
					current = replaceWith;
					return true;
				}
				return true;
			}
			else
			{
				if (bagage.contains(current))
				{
					return false; // Link
				}
				else
				{
					bagage.add(current);
				}
			}

			if (!(current instanceof End))
			{
				if (!updated)
					updated = findAndReplace((current).getNextAction(), replaceWith, hashCode, level + 1);
			}

			if (current instanceof Test)
			{
				if (!updated)
					updated = findAndReplace(((Test) current).getNoAction(), replaceWith, hashCode, level + 1);
				if (!updated)
					updated = findAndReplace(((Test) current).getErrorAction(), replaceWith, hashCode, level + 1);
			}
			else if (current instanceof Menu)
			{
				Menu menu = (Menu) current;
				if (menu.getItems() != null && menu.getItems().size() > 0)
				{
					for (IMenuItem mi : menu.getItems())
					{
						if (mi instanceof MenuItems)
						{
							if (!updated)
								updated = findAndReplace((MenuItems) mi, replaceWith, hashCode, level + 1);
						}
						else
						{
							if (!updated)
								updated = findAndReplace((MenuItem) mi, replaceWith, hashCode, level + 1);
						}

						if (updated)
							break;
					}
				}
			}

		}
		catch (Exception e)
		{
		}

		return updated;
	}

}
