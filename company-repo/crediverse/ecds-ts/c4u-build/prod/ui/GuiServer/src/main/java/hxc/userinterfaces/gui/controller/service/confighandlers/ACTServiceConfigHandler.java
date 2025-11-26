package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import hxc.processmodel.IProcess;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.service.EnhancedCreditTransferService;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.FieldExtractor;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.request.ConfigurableRequestParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class ACTServiceConfigHandler extends BaseServiceConfigHandler
{
	final static Logger logger = LoggerFactory.getLogger(ACTServiceConfigHandler.class);

	@Override
	public long getServiceID()
	{
		return -1103158008836733663L;	// AUTOMATIC_CREDIT_SHARING_ID
	}

	@Override
	public String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user)
			throws Exception
	{
		// Extract Configuration
		extractLocale(ctx, user, session);
		// Service Classes | Variants | TransferMode
		FieldExtractor fe = new FieldExtractor(config, session, ctx)
		{
			@Override
			public void fieldsExtracted(String name, List<BasicConfigurableParm[]> fields)
			{
				logger.info("Extracted Field: " + name);
				if (name.equalsIgnoreCase(EnhancedCreditTransferService.SC_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.SC_INFO_VARIABLE, fields);
				}
				else if (name.equalsIgnoreCase(EnhancedCreditTransferService.VAR_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.VARIANTS_INFO_VARIABLE, fields);
				}
				else if (name.equalsIgnoreCase(EnhancedCreditTransferService.TM_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.TM_INFO_VARIABLE, fields);
				}
				else
					this.getSession().setAttribute(name, fields);
			}

			@Override
			public void objectStructure(String name, String type, ConfigurableResponseParam[] structure)
			{
				if (name.equalsIgnoreCase(EnhancedCreditTransferService.SC_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.SC_STRUCTURE_REF, structure);
				}
				else if (name.equalsIgnoreCase(EnhancedCreditTransferService.VAR_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.VARIANT_STRUCTURE_REF, structure);
				}
				else if (name.equalsIgnoreCase(EnhancedCreditTransferService.TM_FIELD))
				{
					this.getSession().setAttribute(EnhancedCreditTransferService.TM_STRUCTURE_REF, structure);
				}
				else
					this.getSession().setAttribute(name + ComplexConfiguration.STRUCTURE_SUFFIX, structure);
			}

			@Override
			public void nonArrayFieldExtract(String name, ConfigurableResponseParam[] fields)
			{
			}

		};

		// Remove any changes that might have been incurred
		session.removeAttribute(EnhancedCreditTransferService.SC_INFO_VARIABLE_UPDATED);
		session.removeAttribute(EnhancedCreditTransferService.TM_INFO_VARIABLE_UPDATED);
		session.removeAttribute(EnhancedCreditTransferService.VARIANTS_INFO_VARIABLE_UPDATED);
		session.removeAttribute(EnhancedCreditTransferService.USSD_UPDATED);

		fe.findStructuredConfigs();

		session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST, fe.getStructuredFieldList());
		session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST, fe.getUnStructuredFieldList());		

		// Process model
		ConfigurableResponseParam[] fa = (ConfigurableResponseParam[]) config.getParams();
		extractUSSDMenuProcess(session, ctx, fa, ServiceConfigurationLoader.PROCESS_USSD_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_JSON_VARIABLE, 
				ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
		return "enhancedCS/enhancedCreditSharing";
	}

	@Override
	public Configurable processUpdateRequestHandler(HttpSession session, long luid, int iversion,
			Map<String, String[]> parms, User user) throws Exception
	{
		// Populate that which requires update
		List<ConfigurableRequestParam> params = new ArrayList<>();
		if ((session.getAttribute(EnhancedCreditTransferService.SC_INFO_VARIABLE_UPDATED) != null))
			params.add(new ConfigurableRequestParam(EnhancedCreditTransferService.SC_FIELD, (List<IConfigurableParam[]>) session.getAttribute(EnhancedCreditTransferService.SC_INFO_VARIABLE)));
		if ((session.getAttribute(EnhancedCreditTransferService.VARIANTS_INFO_VARIABLE_UPDATED) != null))
			params.add(new ConfigurableRequestParam(EnhancedCreditTransferService.VAR_FIELD, (List<IConfigurableParam[]>) session.getAttribute(EnhancedCreditTransferService.VARIANTS_INFO_VARIABLE)));
		if ((session.getAttribute(EnhancedCreditTransferService.TM_INFO_VARIABLE_UPDATED) != null))
			params.add(new ConfigurableRequestParam(EnhancedCreditTransferService.TM_FIELD, (List<IConfigurableParam[]>) session.getAttribute(EnhancedCreditTransferService.TM_INFO_VARIABLE)));

		String commandField = "Commands";
		if ((session.getAttribute(commandField + ComplexConfiguration.UPDATED_SUFFIX) != null))
			params.add(new ConfigurableRequestParam(commandField, (List<IConfigurableParam[]>) session.getAttribute(commandField)));

		// Persist UssdMenu
		if (session.getAttribute(ServiceConfigurationLoader.USSD_UPDATED) != null)
		{
			IProcess process = (IProcess) session.getAttribute(ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
			ConfigurableResponseParam cp = (ConfigurableResponseParam) session.getAttribute(ServiceConfigurationLoader.PROCESS_USSD_VARIABLE);
			cp.setValue(process.serialize());
			params.add(new ConfigurableRequestParam(cp.getFieldName(), cp));
		}

		if (params.size() > 0)
		{
			ConfigurableRequestParam[] configArray = params.toArray(new ConfigurableRequestParam[params.size()]);
			Configurable confUpdate = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, configArray, false);
			iversion = confUpdate.getVersion();
		}

		Configurable confUpdate = persistReturnCodeConfiguration(user, luid, iversion, parms, "ReturnCodesTexts");
		
		return confUpdate;
	}

}
