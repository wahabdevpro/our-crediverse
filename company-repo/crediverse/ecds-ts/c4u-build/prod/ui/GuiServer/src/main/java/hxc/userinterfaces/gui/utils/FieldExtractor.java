package hxc.userinterfaces.gui.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public abstract class FieldExtractor
{
	private Configurable config;
	private HttpSession session;
	private WebContext webContext;

	private List<String> structuredFieldList;
	private List<String> unstructuredFieldList;

	public FieldExtractor(Configurable config, HttpSession session, WebContext ctx)
	{
		this.config = config;
		this.session = session;
		this.webContext = ctx;
		structuredFieldList = new ArrayList<>();
		unstructuredFieldList = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public void findStructuredConfigs()
	{
		for (int i = 0; i < config.getParams().length; i++)
		{

			IConfigurableParam param = config.getParams()[i];
			ConfigurableResponseParam conParm = (ConfigurableResponseParam) param;

			if (conParm.getStructure() != null && (param.getValue() instanceof List<?>))
			{
				List<BasicConfigurableParm[]> configParams = new ArrayList<>();

				// Extract All groups of properties
				for (IConfigurableParam[] propGroup : (List<IConfigurableParam[]>) param.getValue())
				{
					BasicConfigurableParm[] values = new BasicConfigurableParm[propGroup.length];
					for (int j = 0; j < propGroup.length; j++)
					{
						values[j] = new BasicConfigurableParm(propGroup[j].getFieldName(), propGroup[j].getValue());
					}
					configParams.add(values);
				}

				// Publish results
				fieldsExtracted(conParm.getFieldName(), configParams);
				objectStructure(conParm.getFieldName(), conParm.getValueType(), conParm.getStructure());
				structuredFieldList.add(conParm.getFieldName());
			}
			else if ((param != null) && (param.getValue() instanceof ConfigurableResponseParam[]))
			{
				nonArrayFieldExtract(conParm.getFieldName(), (ConfigurableResponseParam[]) param.getValue());
				unstructuredFieldList.add(conParm.getFieldName());
			}

		}
	}

	private <T> T extractFields(Class cls, IConfigurableParam[] propGroup) throws InstantiationException, IllegalAccessException
	{
		T result = (T) cls.newInstance();
		String fieldName = null;
		for (IConfigurableParam parm : propGroup)
		{
			fieldName = parm.getFieldName().substring(0, 1).toLowerCase() + parm.getFieldName().substring(1);
			try
			{
				Field field = result.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(result, parm.getValue());
			}
			catch (Exception e)
			{
				// Just Ignore the field (quotaID / variantID)
			}
		}
		return result;
	}

	public HttpSession getSession()
	{
		return session;
	}

	public WebContext getWebContext()
	{
		return webContext;
	}

	public List<String> getStructuredFieldList()
	{
		return structuredFieldList;
	}

	public List<String> getUnStructuredFieldList()
	{
		return unstructuredFieldList;
	}

	public abstract void objectStructure(String name, String type, ConfigurableResponseParam[] structure);

	public abstract void fieldsExtracted(String name, List<BasicConfigurableParm[]> fields);

	public abstract void nonArrayFieldExtract(String name, ConfigurableResponseParam[] fields);

}
