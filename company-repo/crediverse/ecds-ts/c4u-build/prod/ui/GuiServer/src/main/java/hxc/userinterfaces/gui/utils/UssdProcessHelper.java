package hxc.userinterfaces.gui.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import hxc.processmodel.IProcess;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.processmodel.LocaleResponse;
import hxc.userinterfaces.gui.processmodel.ModelAttributeInfo;
import hxc.userinterfaces.gui.processmodel.ModelProperty;
import hxc.userinterfaces.gui.processmodel.ModelPropertyGroup;
import hxc.userinterfaces.gui.processmodel.TextData;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.Test;
import hxc.utils.processmodel.ui.UIProperties;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.PropertyInfo;
import hxc.utils.reflection.ReflectionHelper;

public class UssdProcessHelper
{

	/**
	 * 
	 * @param node
	 * @param session
	 * @param sessionActionVariables
	 *            CreditSharingService.PROCESS_ACTION_ACTION_VARIABLES
	 * @return
	 */
	private List<ModelPropertyGroup> extractProperties(Object node, HttpSession session, String PROCESS_ACTION_ACTION_VARIABLES)
	{
		Map<String, ModelPropertyGroup> groups = new TreeMap<>();
		@SuppressWarnings("unchecked")
		Map<Integer, String> actionVariables = (Map<Integer, String>) session.getAttribute(PROCESS_ACTION_ACTION_VARIABLES);
		try
		{
			ClassInfo info = ReflectionHelper.getClassInfo(node.getClass());
			for (PropertyInfo pi : info.getProperties().values())
			{
				Method getter = pi.getGetterMethod();
				if (getter != null)
				{
					Annotation anno = getter.getAnnotation(UIProperties.class);
					if (anno != null)
					{
						UIProperties up = (UIProperties) anno;
						if (!groups.containsKey(up.category()))
						{
							groups.put(up.category(), new ModelPropertyGroup(up.category()));
						}
						ModelPropertyGroup group = groups.get(up.category());
						String parmName = methodToProperty(pi.getName());
						Object value = pi.get(node);
						String strValue = up.value();
						if (strValue.length() == 0 && value != null)
						{
							if (value instanceof Action)
								strValue = actionVariables.get(System.identityHashCode(value));
							else if (value instanceof ITexts)
								strValue = ((ITexts) value).getModelText();
							// else if (value instanceof MessageMap)
							// strValue = ((MessageMap) value).getDefaultMessage().getModelText();
							else
								strValue = value.toString();
						}
						else
						{
							if (parmName.equalsIgnoreCase("NextAction") || parmName.equalsIgnoreCase("NoAction") || parmName.equalsIgnoreCase("YesAction"))
							{
								strValue = "TBD";
							}
						}
						if (strValue == null)
						{
							strValue = "";
						}

						ModelProperty prop = new ModelProperty(parmName, parmName, strValue, up.editable());
						group.getProperties().add(prop);
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return new ArrayList<ModelPropertyGroup>(groups.values());
	}

	private String methodToProperty(String name)
	{
		if (name.startsWith("get"))
		{
			return name.substring(3);
		}
		else if (name.startsWith("is"))
		{
			return name.substring(2);
		}
		return name;
	}

	/**
	 * 
	 * @param process
	 *            Process with start in it
	 * @param instructions
	 *            e.g. SYYYNI10 > Start > Next > Next > NoAction > Item 10
	 * @return
	 */
	private Action findAction(IProcess process, String nodeId)
	{

		Action result = (Action) process.getStart();
		String curNode = null;
		nodeId = nodeId.substring(1); // Remove "S" for start
		int index = -1;

		while (nodeId.length() > 0)
		{
			// Calculate next path
			curNode = nodeId.substring(0, 1);
			String newNodeId = nodeId.substring(1);
			if (curNode.equals("I"))
			{
				index = extractNumber(newNodeId);
				newNodeId = nodeId.substring((curNode + index).length());
			}
			else
			{
				index = -1;
			}

			try
			{

				if (curNode.equals("Y"))
				{
					result = result.getNextAction();
				}
				else if (curNode.equals("N"))
				{
					result = ((Test) result).getNoAction();
				}
				else if (curNode.equals("E"))
				{
					result = ((Test) result).getErrorAction();
				}
				if (curNode.equals("I"))
				{
					Menu menu = (Menu) result;
					result = (Action) menu.getItems().get(index);
				}

			}
			catch (Exception e)
			{
			}

			// Next round
			nodeId = newNodeId;
		}

		return result;
	}

	public String extractNodeData(HttpSession session, String nodeId, String PROCESS_ACTION_PROCESS_VARIABLE, String PROCESS_ACTION_ACTION_VARIABLES)
	{
		// NodeId will be in the form "act_SYYI0"
		String actionPosition = nodeId.substring(nodeId.indexOf('_') + 1);
		IProcess process = (IProcess) session.getAttribute(PROCESS_ACTION_PROCESS_VARIABLE);
		Action nodeAction = findAction(process, actionPosition);
		List<ModelPropertyGroup> props = extractProperties(nodeAction, session, PROCESS_ACTION_ACTION_VARIABLES);
		return ModelPropertyGroup.ModelGroupArrayToString(actionPosition, props);
	}

	private TextData extractTextsData(String code, ITexts texts)
	{
		TextData td = new TextData();
		td.setCode(code);
		td.setText(new String[IPhrase.MAX_LANGUAGES + 1]);
		td.getText()[0] = (texts.getModelText() == null) ? "" : texts.getModelText();
		for (int i = 1; i <= IPhrase.MAX_LANGUAGES; i++)
		{
			td.getText()[i] = (texts.getText(i) == null) ? "" : texts.getText(i);
		}
		return td;
	}

	private PropertyInfo getPropertyFromAction(Action action, String attributeId)
	{
		PropertyInfo result = null;
		ClassInfo info = ReflectionHelper.getClassInfo(action.getClass());
		for (PropertyInfo pi : info.getProperties().values())
		{
			String parmName = methodToProperty(pi.getName());
			if (attributeId.equalsIgnoreCase(parmName))
			{
				result = pi;
				break;
			}
		}
		return result;
	}

	public String savePropertyData(HttpSession session, HttpServletRequest request, String nodeId, String attributeId, String PROCESS_ACTION_PROCESS_VARIABLE)
	{
		// First Extract nodeId / attributeId
		GuiUpdateResponse result = null;
		try
		{

			IProcess process = (IProcess) session.getAttribute(PROCESS_ACTION_PROCESS_VARIABLE);
			Action action = findAction(process, nodeId);
			PropertyInfo pi = getPropertyFromAction(action, attributeId);

			Object value = pi.get(action);
			if (value != null)
			{
				int langId = 0;
				Map<ReturnCodes, Texts> returnCodeMappings = null;
				Set<ReturnCodes> attribs = null;
				String text = null;
				String attrib = null;

				if (value instanceof ITexts)
				{

					for (String parm : request.getParameterMap().keySet())
					{
						String[] parts = parm.split("_"); // parts[0]=nodeId | parts[1]=ITexts|MessageMap | parts[2]=attribute | parts[3]=languageID
						if (parts.length >= 4)
						{
							attrib = parts[2];

							if (parts[0].equals(nodeId))
							{

								try
								{
									langId = Integer.parseInt(parts[3]);
									text = request.getParameter(parm);

									if (value instanceof ITexts)
									{
										((ITexts) value).setText(langId, text);
									}
								}
								catch (Exception e)
								{

								}

							}
						}
					}
				}

				// Persist Actions to session
				session.setAttribute(PROCESS_ACTION_PROCESS_VARIABLE, process);
				result = new GuiUpdateResponse(OperationStatus.pass, "Message updated");
				session.setAttribute(ServiceConfigurationLoader.USSD_UPDATED, true);
			}
		}
		catch (Exception e)
		{
			result = new GuiUpdateResponse(OperationStatus.fail, String.format("Saving message failed (%s)", e.getMessage()));
		}
		if (result == null)
		{
			result = new GuiUpdateResponse(OperationStatus.fail, "Message not updated (nothing found)");
		}
		return result.toString();
	}

	public static int extractNumber(String key)
	{
		boolean isNumber = true;
		int indexLast = 0;

		while (isNumber && (indexLast < key.length()))
		{
			char c = key.charAt(indexLast);
			isNumber = c >= '0' && c <= '9';
			if (isNumber)
				indexLast++;
		}

		int id = -1;
		try
		{
			id = Integer.parseInt(key.substring(0, indexLast));
		}
		catch (Exception e)
		{
		}

		return id;
	}

	private String extractLastPartOfClass(Action action)
	{
		String name = action.getClass().toString();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name;
	}

	public String extractPropertyData(HttpSession session, String nodeId, String attributeId, String PROCESS_ACTION_PROCESS_VARIABLE)
	{
		ModelAttributeInfo minfo = null;

		try
		{
			IProcess process = (IProcess) session.getAttribute(PROCESS_ACTION_PROCESS_VARIABLE);
			Action action = findAction(process, nodeId);
			PropertyInfo pi = getPropertyFromAction(action, attributeId);

			minfo = new ModelAttributeInfo(nodeId, attributeId);
			minfo.setnType(extractLastPartOfClass(action));
			Object value = pi.get(action);
			if (value != null)
			{
				if (value instanceof ITexts)
				{
					// General Message Text
					minfo.setPropertyType("ITexts");
					TextData td = extractTextsData("msg", (ITexts) value);
					minfo.setData(new TextData[] { td });
				}

			}
		}
		catch (Exception e)
		{
		}

		return minfo.toString();
	}

	public void updateAction(HttpSession session, String nodeId, HttpServletRequest request, String PROCESS_ACTION_PROCESS_VARIABLE)
	{
		String actionCode = nodeId.substring(nodeId.indexOf('_') + 1);
		IProcess process = (IProcess) session.getAttribute(PROCESS_ACTION_PROCESS_VARIABLE);
		Action editAction = findAction(process, actionCode);

		if (editAction != null)
		{

			for (String key : request.getParameterMap().keySet())
			{
				int index = -1;
				String value = request.getParameter(key);
				String[] parts = key.split("_");

				try
				{
					if (parts.length == 2)
						index = Integer.parseInt(parts[1]);
				}
				catch (Exception e)
				{
				}

				index++;

				if (key.startsWith("menu_"))
				{
					setActionText(editAction, value, index);
				}
				else if (key.startsWith("menuempty_"))
				{
					setActionEmptyText(editAction, value, index);
				}
			}

			// Save object back
			session.setAttribute(PROCESS_ACTION_PROCESS_VARIABLE, process);
		}
	}

	private void setActionText(Action action, String text, int index)
	{
		if (action instanceof Menu)
		{
			((Menu) action).getCaption().setText(index, text);
		}
		else if (action instanceof MenuItem)
		{
			((MenuItem) action).getText().setText(index, text);
		}
		else if (action instanceof MenuItems)
		{
			((MenuItems<?>) action).getText().setText(index, text);
		}
	}

	private void setActionEmptyText(Action action, String text, int index)
	{
		if (action instanceof MenuItems)
		{
			((MenuItems<?>) action).getEmptyText().setText(index, text);
		}
	}

	private JsonArray extractTexts(ITexts texts)
	{
		JsonArray jarr = new JsonArray();
		jarr.add(new JsonPrimitive(texts.getModelText()));
		for (int i = 1; i < 5; i++)
		{
			if (texts.getText(i) != null)
				jarr.add(new JsonPrimitive(texts.getText(i)));
			else
				jarr.add(new JsonPrimitive(""));
		}
		return jarr;
	}

	public String extractSystemLanguageInfo(User user)
	{
		String result = null;
		try
		{
			GetLocaleInformationResponse locResp = UiConnectionClient.getInstance().extractLocaleInformation(user);
			LocaleResponse resp = new LocaleResponse();
			resp.setLang(locResp.getLanguages().toArray(new String[locResp.getLanguages().size()]));
			resp.setApha(locResp.getAlphabet().toArray(new String[locResp.getAlphabet().size()]));
			resp.convertShortCodesToFullLanuage();
			result = resp.toString();
		}
		catch (Exception e)
		{
		}
		return result;
	}

}
