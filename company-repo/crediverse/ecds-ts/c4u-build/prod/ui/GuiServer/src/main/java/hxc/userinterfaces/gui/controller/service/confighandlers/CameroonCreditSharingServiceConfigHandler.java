package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.service.CreditSharingService;
import hxc.userinterfaces.gui.data.LocaleInfo;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.processmodel.LocaleResponse;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.FieldExtractor;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public class CameroonCreditSharingServiceConfigHandler extends BaseServiceConfigHandler
{

	@Override
	public long getServiceID()
	{
		return 3851988109129936993L;	// CREDIT_SHARING_SERVICE_ID
	}

	@Override
	public String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user)
			throws Exception
	{
		// Process model
		ConfigurableResponseParam[] fa = (ConfigurableResponseParam[]) config.getParams();
		extractUSSDMenuProcess(session, ctx, fa, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, 
				ServiceConfigurationLoader.PROCESS_ACTION_JSON_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES, 
				ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);

		// Locale for texts
		GetLocaleInformationResponse locale = UiConnectionClient.getInstance().extractLocaleInformation(user);
		ArrayList<LocaleInfo> languages = new ArrayList<>();
		for (int i = 0; i < locale.getLanguages().size(); i++)
		{
			languages.add(i, GuiUtils.extractLanguageInfo(locale.getLanguage(i)));
		}
		// Note that the "message/texts" variables are in the "args" for each action
		ctx.setVariable("langs", languages);
		session.setAttribute(CURRENCY_SYMBOLS, locale.getCurrencyCode());

		// Bundles/Quotas | Service Classes | Variants
		FieldExtractor fe = new FieldExtractor(config, session, ctx)
		{
			@Override
			public void fieldsExtracted(String name, List<BasicConfigurableParm[]> fields)
			{
				if (name.equalsIgnoreCase(CreditSharingService.QUOTA_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.QUOTA_INFO_VARIABLE, fields);
				}
				else if (name.equalsIgnoreCase(CreditSharingService.SC_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.SC_INFO_VARIABLE, fields);
				}
				else if (name.equalsIgnoreCase(CreditSharingService.VAR_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.VARIANTS_INFO_VARIABLE, fields);
				}
			}

			@Override
			public void objectStructure(String name, String type, ConfigurableResponseParam[] structure)
			{
				if (name.equalsIgnoreCase(CreditSharingService.QUOTA_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.QUOTA_STRUCTURE_REF, structure);
				}
				else if (name.equalsIgnoreCase(CreditSharingService.SC_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.SC_STRUCTURE_REF, structure);
				}
				else if (name.equalsIgnoreCase(CreditSharingService.VAR_FIELD))
				{
					this.getSession().setAttribute(CreditSharingService.VARIANT_STRUCTURE_REF, structure);
				}
			}

			@Override
			public void nonArrayFieldExtract(String name, ConfigurableResponseParam[] fields)
			{
				// TODO Auto-generated method stub
			}

		};

		// Remove any changes that might have been incurred
		session.removeAttribute(CreditSharingService.SC_INFO_VARIABLE_UPDATED);
		session.removeAttribute(CreditSharingService.QUOTA_INFO_VARIABLE_UPDATED);
		session.removeAttribute(CreditSharingService.VARIANTS_INFO_VARIABLE_UPDATED);
		session.removeAttribute(ServiceConfigurationLoader.USSD_UPDATED);

		// Language information for Texts
		GetLocaleInformationResponse locResp = UiConnectionClient.getInstance().extractLocaleInformation(user);
		LocaleResponse resp = new LocaleResponse();
		resp.setLang(locResp.getLanguages().toArray(new String[locResp.getLanguages().size()]));
		resp.convertShortCodesToFullLanuage();
		ctx.setVariable("directions", resp.getDir());
		ctx.setVariable("languages", resp.getLang());

		fe.findStructuredConfigs();

		return "creditsharing/creditSharing";
	}

	@Override
	public Configurable processUpdateRequestHandler(HttpSession session, long luid, int iversion,
			Map<String, String[]> parms, User user) throws Exception
	{
		Configurable updated = null;
		
		// Must Update?
		boolean updateServiceClass = (session.getAttribute(CreditSharingService.SC_INFO_VARIABLE_UPDATED) != null);
		boolean updateQuotas = (session.getAttribute(CreditSharingService.QUOTA_INFO_VARIABLE_UPDATED) != null);
		boolean updateVariants = (session.getAttribute(CreditSharingService.VARIANTS_INFO_VARIABLE_UPDATED) != null);
		boolean updateUssd = (session.getAttribute( ServiceConfigurationLoader.USSD_UPDATED) != null);

		// Persist service Classes
		if (updateServiceClass)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(CreditSharingService.SC_INFO_VARIABLE);
			updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, CreditSharingService.SC_FIELD, toSave, false);
			iversion = updated.getVersion();
			session.removeAttribute(CreditSharingService.SC_INFO_VARIABLE_UPDATED);
		}
		// Persist Quotas
		if (updateQuotas)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(CreditSharingService.QUOTA_INFO_VARIABLE);
			updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, CreditSharingService.QUOTA_FIELD, toSave, false);
			iversion = updated.getVersion();
			session.removeAttribute(CreditSharingService.QUOTA_INFO_VARIABLE_UPDATED);
		}
		// Persist Variants
		if (updateVariants)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(CreditSharingService.VARIANTS_INFO_VARIABLE);
			updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, CreditSharingService.VAR_FIELD, toSave, false);
			iversion = updated.getVersion();
			session.removeAttribute(CreditSharingService.VARIANTS_INFO_VARIABLE_UPDATED);
		}

		// Persist UssdMenu
		Configurable upd = udpateUSSDMenuProcess(session, user, iversion, luid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, ServiceConfigurationLoader.USSD_UPDATED);
		if (upd != null)
		{
			iversion = upd.getVersion();
		}

		updated = persistReturnCodeConfiguration(user, luid, iversion, parms, "ReturnCodesTexts");
		return updated;
	}

}
