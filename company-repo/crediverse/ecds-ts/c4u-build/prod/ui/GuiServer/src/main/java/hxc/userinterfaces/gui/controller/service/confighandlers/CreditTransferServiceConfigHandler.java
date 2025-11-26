package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class CreditTransferServiceConfigHandler extends BaseServiceConfigHandler
{

	@Override
	public long getServiceID()
	{
		return 52472278881652629L;	// CREDIT_TRANSFER_ID
	}

	@Override
	public String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user)
			throws Exception
	{
		// Extract Configuration
		extractAdvancedConfiguration(session, ctx, user, config);

		// Process model
		ConfigurableResponseParam[] fa = (ConfigurableResponseParam[]) config.getParams();
		extractUSSDMenuProcess(session, ctx, fa, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_JSON_VARIABLE, 
				ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
		return "credittransfer/credittransfer";
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
