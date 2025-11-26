package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.service.GroupSharedAccounts;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.processmodel.LocaleResponse;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.FieldExtractor;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public class GroupSharedAccountHandler extends BaseServiceConfigHandler
{
//	private static final long GROUP_SHARED_ACCOUNT = -9213363036854775808L;

	@Override
	public long getServiceID()
	{
		return -9213363036854775808L;
	}
	
	@Override
	public String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user) throws Exception
	{
		// Extract Locale Configuration
		extractLocale(ctx, user, session);
				
		ConfigurableResponseParam[] fa = (ConfigurableResponseParam[]) config.getParams();

		// Process model
		extractUSSDMenuProcess(session, ctx, fa, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_JSON_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);

		// Bundles/Quotas | Service Classes | Variants
		FieldExtractor fe = new FieldExtractor(config, session, ctx)
		{
			@Override
			public void fieldsExtracted(String name, List<BasicConfigurableParm[]> fields)
			{
				if (name.equalsIgnoreCase(GroupSharedAccounts.QUOTA_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.QUOTA_FIELD, fields);
				}
				else if (name.equalsIgnoreCase(GroupSharedAccounts.SC_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.SC_FIELD, fields);
				}
				else if (name.equalsIgnoreCase(GroupSharedAccounts.VAR_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.VAR_FIELD, fields);
				}
			}

			@Override
			public void objectStructure(String name, String type, ConfigurableResponseParam[] structure)
			{
				if (name.equalsIgnoreCase(GroupSharedAccounts.QUOTA_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.QUOTA_FIELD_STRUCT, structure);
				}
				else if (name.equalsIgnoreCase(GroupSharedAccounts.SC_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.SC_FIELD_STRUCT, structure);
				}
				else if (name.equalsIgnoreCase(GroupSharedAccounts.VAR_FIELD))
				{
					this.getSession().setAttribute(GroupSharedAccounts.VAR_FIELD_STRUCT, structure);
				}
			}

			@Override
			public void nonArrayFieldExtract(String name, ConfigurableResponseParam[] fields)
			{
				// TODO Auto-generated method stub
			}

		};

		// Remove any changes that might have been incurred
		session.removeAttribute(GroupSharedAccounts.SC_FIELD_UPDATED);
		session.removeAttribute(GroupSharedAccounts.QUOTA_FIELD_UPDATED);
		session.removeAttribute(GroupSharedAccounts.VAR_FIELD_UPDATED);
		session.removeAttribute(ServiceConfigurationLoader.USSD_UPDATED);

		// Language information for Texts
		GetLocaleInformationResponse locResp = UiConnectionClient.getInstance().extractLocaleInformation(user);
		LocaleResponse resp = new LocaleResponse();
		resp.setLang(locResp.getLanguages().toArray(new String[locResp.getLanguages().size()]));
		resp.convertShortCodesToFullLanuage();
		ctx.setVariable("directions", resp.getDir());
		ctx.setVariable("languages", resp.getLang());

		fe.findStructuredConfigs();		
		return "groupsharedaccounts/creditSharing";
	}

	@Override
	@SuppressWarnings("unchecked")	// For casting to List<IConfigurableParam[]>
	public Configurable processUpdateRequestHandler(HttpSession session, long luid, int iversion, Map<String, String[]> parms, User user) throws Exception
	{
		Configurable result = null;

		// Must Update?
		boolean updateServiceClass = (session.getAttribute(GroupSharedAccounts.SC_FIELD_UPDATED) != null);
		boolean updateQuotas = (session.getAttribute(GroupSharedAccounts.QUOTA_FIELD_UPDATED) != null);
		boolean updateVariants = (session.getAttribute(GroupSharedAccounts.VAR_FIELD_UPDATED) != null);
		boolean updateUssd = (session.getAttribute(ServiceConfigurationLoader.USSD_UPDATED) != null);

		// Persist service Classes
		if (updateServiceClass)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.SC_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.SC_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.SC_FIELD_UPDATED);
		}

		// Persist Quotas
		if (updateQuotas)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.QUOTA_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.QUOTA_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.QUOTA_FIELD_UPDATED);
		}

		// Persist Variants
		if (updateVariants)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.VAR_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.VAR_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.VAR_FIELD_UPDATED);
		}

		// Persist UssdMenu
		if (updateUssd)
		{
			Configurable upd = udpateUSSDMenuProcess(session, user, iversion, luid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, ServiceConfigurationLoader.USSD_UPDATED);
			if (upd != null)
			{
				iversion = upd.getVersion();
			}
		}

		// Persist Result Texts
		return persistReturnCodeConfiguration(user, luid, iversion, parms, "ReturnCodesTexts");
	}

}
