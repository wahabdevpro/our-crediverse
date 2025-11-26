package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class FriendsAndFamilyServiceServiceConfigHandler extends BaseServiceConfigHandler 
{

	@Override
	public long getServiceID()
	{
		return -1086486634276858629L;
	}

	@Override
	public String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user) throws Exception
	{
		extractAdvancedConfiguration(session, ctx, user, config);
		return "friendsandfamily/friendsandfamily";
	}

	@Override
	public Configurable processUpdateRequestHandler(HttpSession session, long luid, int iversion,
			Map<String, String[]> parms, User user) throws Exception
	{
		Configurable updated = null;
		// Iterate through complex fields to check if there is an update
		if (session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST) != null)
		{
			List<String> fieldNames = (List<String>) session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST);
			for (String fieldName : fieldNames)
			{
				if (session.getAttribute(fieldName + ComplexConfiguration.UPDATED_SUFFIX) != null)
				{
					List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(fieldName);
					updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, fieldName, toSave, false);
					iversion = updated.getVersion();
					session.removeAttribute(fieldName + ComplexConfiguration.UPDATED_SUFFIX);
				}
			}
		}

		// Now save changes to UNSTRUCTURE Complex fields
		if (session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST) != null)
		{
			updated = saveUnstructuredConfiguration(session, user, luid, iversion, parms);
			if (updated != null)
			{
				iversion = updated.getVersion();
			}
		}

		// Return Code Texts ?!?
		updated = persistReturnCodeConfiguration(user, luid, iversion, parms, "ReturnCodesTexts");
		return updated;
	}

}
