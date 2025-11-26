package hxc.userinterfaces.gui.processmodel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.processmodel.IProcess;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.End;
import hxc.utils.processmodel.ErrorDisplay;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.Start;
import hxc.utils.processmodel.Test;

public class ProcessModelTreeGenerator
{
	public static enum ActionType
	{
		START, NEXT, ITEM, YES_ACTION, NO_ACTION, ERROR
	}

	int linkCount = 1;
	int MAX_TREE_DEPTH = 100;

	private String createIconFromType(Action action, boolean isLink)
	{
		String icon = "StartIcon";

		if (action == null)
		{
			icon = "TBDIcon";
		}
		else if (action instanceof Start)
		{
			icon = "StartIcon";
		}
		else if (action instanceof Test)
		{
			icon = "ExclGatewayIcon";
		}
		else if (action instanceof Menu)
		{
			icon = "UserActivityIcon";
		}
		else if (action instanceof MenuItem)
		{
			icon = "MenuItemIcon";
		}
		else if (action instanceof MenuItems)
		{
			icon = "MenuItemsIcon";
		}
		else if (action instanceof ErrorDisplay)
		{
			icon = "ErrorIntermediateIcon";
		}
		else if (action instanceof End)
		{
			icon = "MessageEndIcon";
		}
		else
		{
			icon = "ServiceActivityIcon";
		}
		if (isLink)
		{
			icon = "To" + icon;
		}
		return icon;
	}

	public String createNameFromAction(Action action, Map<Integer, String> actionVariables)
	{

		if (action == null)
		{
			return "TBD";
		}
		String name = action.getClass().toString();
		name = name.substring(name.lastIndexOf('.') + 1);
		name = name.substring(0, 1).toLowerCase() + name.substring(1);

		if (name.equalsIgnoreCase("ussdstart"))
		{
			return name;
		}

		int count = 1;
		while (actionVariables.containsValue(name + count))
		{
			count++;
		}

		name += count;
		actionVariables.put(System.identityHashCode(action), name);

		return name;
	}

	private String getCaption(Action action)
	{
		String caption = null;
		if (action != null)
		{
			if (action instanceof Menu)
			{
				caption = "[" + ((Menu) action).getCaption().getModelText() + "]";
			}
			else if (action instanceof MenuItem)
			{
				caption = "[" + ((MenuItem) action).getText().getModelText() + "]";
			}
			else if (action instanceof MenuItems)
			{
				caption = "[" + ((MenuItems<?>) action).getText().getModelText() + "]";
			}
		}

		return caption;
	}

	private String buildTreeItemJson(String id, String parent, Action action, int level, ActionType actionType, Map<Integer, String> actionVariables, boolean isLink)
	{
		JsonObject job = new JsonObject();
		job.add("id", new JsonPrimitive(id));
		job.add("parent", new JsonPrimitive(parent));

		// Text
		StringBuilder label = new StringBuilder();
		switch (actionType)
		{
			case START:
				label.append("Start:");
				break;
			case NEXT:
				label.append("Next:");
				break;
			case NO_ACTION:
				label.append("No:");
				break;
			case YES_ACTION:
				label.append("Yes:");
				break;
			case ERROR:
				label.append("Error:");
				break;
			case ITEM:
				label.append("Item:");
				break;
			default:
				label.append("?:");
				break;
		}
		label.append(" ").append(createNameFromAction(action, actionVariables));
		String caption = getCaption(action);
		if (caption != null)
		{
			label.append(" ").append(caption);
		}
		job.add("text", new JsonPrimitive(label.toString()));

		job.add("icon", new JsonPrimitive(createIconFromType(action, isLink)));

		JsonObject state = new JsonObject();
		state.add("opened", new JsonPrimitive((level < 2)));
		job.add("state", state);

		return job.toString() + ",\n";
	}

	// Use when initialized and building the model for the menu
	public void extractProcessModel(WebContext ctx, HttpSession session, String jsonVariableName, String serializedStart, String actionVariablesSessionVariable, String actionProcessSessionVariable)
	{
		List<Action> processActions = new LinkedList<>();
		IProcess process = Start.deserialize(serializedStart);
		Action start = (Action) process.getStart();
		// ProcessModelTreeGenerator pmg = new ProcessModelTreeGenerator();
		Map<Integer, String> actionVariables = new HashMap<>();
		String json = buildTreeJson(start, processActions, "#", actionVariables);

		ctx.setVariable(jsonVariableName, json);
		// session.setAttribute(sessionVariable, processActions);
		session.setAttribute(actionVariablesSessionVariable, actionVariables);
		session.setAttribute(actionProcessSessionVariable, process);
	}

	/**
	 * Start Call
	 */
	public String buildTreeJson(Action action, List<Action> bagage, String parentId, Map<Integer, String> actionVariables)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"data\" : [");
		String job = buildTreeData(action, ActionType.START, parentId, actionVariables, 0, "", -1);
		if (job != null && job.length() > 0)
		{
			job = job.substring(0, job.length() - 2); // Remove extra commas
			sb.append(job);
		}
		sb.append("]}");
		actionKeys = null;
		return sb.toString();
	}

	private Map<String, Action> actionKeys = new HashMap<>(); // New bagage

	public String buildTreeData(Action action, ActionType callActionType, String parentId, Map<Integer, String> actionVariables, int level, String calledFromCode, int itemIndex)
	{
		// Safety net (shouldn't need)
		if (level > MAX_TREE_DEPTH)
		{
			String key = "ERR" + linkCount++;
			String treeItem = buildTreeItemJson(key, (parentId.equals("#") ? "" : "act_") + parentId, action, level, callActionType, actionVariables, false);
			return treeItem;
		}

		// Build up link with simple code (Y(Yes/Next), N(No), E(Error), I(Item followed by number))
		String thisNodeId = (parentId.equals("#") ? "S" : parentId) + calledFromCode + ((itemIndex >= 0) ? itemIndex : "");

		// if (actionKeys.containsKey(thisNodeId))
		// {
		// Posible error with node
		// }

		if (action != null)
		{

			if (actionKeys.containsValue(action))
			{
				StringBuilder key = new StringBuilder();
				String nodeId = null;
				for (String ky : actionKeys.keySet())
				{
					if (actionKeys.get(ky).equals(action))
					{
						nodeId = ky;
						break;
					}
				}
				key.append("LNK").append(linkCount++).append("_").append(nodeId);
				String treeItem = buildTreeItemJson(key.toString(), "act_" + parentId, action, level, callActionType, actionVariables, true);
				return treeItem;
			}
			else
				actionKeys.put(thisNodeId, action);
		}
		else
		{
			String key = "TBD" + linkCount++;
			String treeItem = buildTreeItemJson(key, (parentId.equals("#") ? "" : "act_") + parentId, action, level, callActionType, actionVariables, false);
			return treeItem;
		}

		// Print current Action
		StringBuilder sb = new StringBuilder();
		String treeItem = buildTreeItemJson("act_" + thisNodeId, (parentId.equals("#") ? "" : "act_") + parentId, action, level, callActionType, actionVariables, false);
		if (treeItem != null)
		{
			sb.append(treeItem);
		}

		level = level + 1;

		if (action instanceof Menu)
		{
			Menu menu = (Menu) action;
			if (menu.getItems() != null && menu.getItems().size() > 0)
			{
				for (int i = 0; i < menu.getItems().size(); i++)
				{

					treeItem = buildTreeData((Action) menu.getItems().get(i), ActionType.ITEM, thisNodeId, actionVariables, level, "I", i);
					if (treeItem != null)
					{
						sb.append(treeItem);
					}
				}
			}
		}

		if (!(action instanceof End))
		{
			ActionType actionType = ActionType.NEXT;
			if ((action != null) && (action instanceof hxc.utils.processmodel.Test))
			{
				actionType = ActionType.YES_ACTION;
			}
			treeItem = buildTreeData(action.getNextAction(), actionType, thisNodeId, actionVariables, level, "Y", -1);
			if (treeItem != null)
			{
				sb.append(treeItem);
			}
		}

		if (action instanceof hxc.utils.processmodel.Test)
		{
			hxc.utils.processmodel.Test testRef = (hxc.utils.processmodel.Test) action;

			treeItem = buildTreeData(testRef.getNoAction(), ActionType.NO_ACTION, thisNodeId, actionVariables, level, "N", -1);
			if (treeItem != null)
			{
				sb.append(treeItem);
			}

			treeItem = buildTreeData(testRef.getErrorAction(), ActionType.ERROR, thisNodeId, actionVariables, level, "E", -1);
			if (treeItem != null)
			{
				sb.append(treeItem);
			}
		}

		return sb.toString();
	}
}
