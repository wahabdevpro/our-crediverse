package hxc.connectors.ui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.connectors.ui.utils.UiConnectorUtils;
import hxc.connectors.vas.VasCommand;
import hxc.processmodel.IProcess;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.notification.Texts;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.SupplierOnly;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateTime;
import hxc.utils.calendar.TimeUnits;
import hxc.utils.processmodel.Start;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.PropertyInfo;
import hxc.utils.reflection.ReflectionHelper;

public class UiUtils
{
	final static Logger logger = LoggerFactory.getLogger(UiUtils.class);

	public <T extends Object> void deepCopy(T from, T to)
	{
		Class<? extends Object> fromClass = from.getClass();
		Field[] fromFields = fromClass.getDeclaredFields();

		Class<? extends Object> tooClass = to.getClass();
		Field[] tooFields = tooClass.getDeclaredFields();

		if (fromFields != null && tooFields != null)
		{
			for (Field tooF : tooFields)
			{
				try
				{
					Field fromF = fromClass.getDeclaredField(tooF.getName());
					if (fromF.getType().equals(tooF.getType()))
					{
						tooF.setAccessible(true);
						fromF.setAccessible(true);
						Object value = fromF.get(from);
						tooF.set(to, value);
					}
				}
				catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
				{
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T configurableParamsToObject(Class<?> clsType, IConfigurableParam[] parms, Object parent) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ValidationException, NoSuchMethodException, SecurityException, InstantiationException
	{
		String lastField = null;
		T result = null;
		boolean processFurtherIfError = true;

		if (ReturnCodeTexts.class.isAssignableFrom(clsType))
		{
			result = (T) parms[0].getValue();
		}
		else
		{
			Constructor<?> constructor = clsType.getDeclaredConstructor();
			constructor.setAccessible(true);
			result = (T) constructor.newInstance();

			ClassInfo ci = ReflectionHelper.getClassInfo(clsType);
			for (IConfigurableParam parm : parms)
			{
				processFurtherIfError = false;
				PropertyInfo pi = ci.getProperties().get(parm.getFieldName());
				if (pi != null)
				{
					lastField = pi.getName();
					try
					{
						// Check if there is a setter
						if (pi.getSetterMethod() != null)
						{
							if ((parm.getValue() != null) && (parm.getValue() instanceof List))
							{
								List<IConfigurableParam[]> list = (List<IConfigurableParam[]>) parm.getValue();
								Class<?> type = pi.getSetterMethod().getParameterTypes()[0].getComponentType();
								Object[] arr = (Object[]) Array.newInstance(type, list.size());

								// pi.set(result, Array.newInstance(type, list.size()));

								Object obj = null;
								for (int ind = 0; ind < list.size(); ind++)
								{
									lastField = String.format("%s[%d]", pi.getName(), ind);
									obj = configurableParamsToObject(type, list.get(ind), parent);
									if (obj != null)
									{
										Array.set(arr, ind, obj);
									}
								}
								pi.set(result, arr);
							}
							else if ((parm.getValue() != null) && (pi.getGetterMethod().getReturnType().isEnum()))
							{
								pi.set(result, Enum.valueOf(((Class<Enum>) pi.getGetterMethod().getReturnType()), parm.getValue().toString()));
							}
							else if ((parm.getValue() != null) && (IPhrase.class.isAssignableFrom(pi.getGetterMethod().getReturnType())))
							{
								pi.set(result, parm.getValue());
							}
							else
							{
								processFurtherIfError = true;
								pi.set(result, parm.getValue());
							}

						}
					}
					catch (Exception e)
					{
						if (e instanceof NullPointerException)
						{
							throw new ValidationException(String.format("Property %s found to be NULL", pi.getName()));
						}
						else if ((e instanceof InvocationTargetException) && (e.getCause() instanceof ValidationException))
						{
							throw ValidationException.createFieldValidationException(lastField, ((ValidationException) e.getCause()).getMessage());
						}
						else if (e instanceof ValidationException)
						{
							ValidationException ve = (ValidationException) e;

							String field = (ve.getField() != null) ? String.format("%s.%s", lastField, ve.getField()) : lastField;
							throw ValidationException.createFieldValidationException(field, ve.getMessage());
						}
						else
						{
							if (processFurtherIfError)
							{
								Class<?> parmType = pi.getSetterMethod().getParameterTypes()[0];
								if (!parmType.equals(String.class))
								{
									Method setter = pi.getSetterMethod();
									String value = (String) parm.getValue();
									UiConnectorUtils.setUsingString(result, value, setter);
								}
								else
								{
									if (e instanceof InvocationTargetException)
									{
										Throwable target = ((InvocationTargetException) e).getTargetException();
										throw new ValidationException(String.format("%s [%s]", target.getMessage(), parm.getValue()));
									}
								}
							}
							else
							{
								throw new ValidationException(String.format("Property %s Problem: %s", pi.getName(), e.getCause()));
							}

						}

					}
				}

			}
		}

		return result;
	}

	public void cloneNotificaitons(IServiceBus esb, IConfiguration fromConfiguration, IConfiguration toConfiguration)
	{
		try
		{
			if (fromConfiguration.getNotifications() != null && fromConfiguration.getNotifications().getNotificationIds() != null)
			{
				for (int notID : fromConfiguration.getNotifications().getNotificationIds())
				{
					for (int langIndex = 1; langIndex <= esb.getLocale().getMaxLanguages(); langIndex++)
					{
						try
						{
							toConfiguration.getNotifications().getNotification(notID).setText(langIndex, fromConfiguration.getNotifications().getNotification(notID).getText(langIndex));
						}
						catch (Exception e)
						{
						}
					}
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Object> T clone(T from, Object parent, Object caller) throws Exception
	{
		if (from == null)
			return null;

		T result = null;
		try
		{
			Constructor<?> constructor = null;
			try
			{
				constructor = from.getClass().getDeclaredConstructor(parent.getClass());
			}
			catch (Exception ex)
			{
				try
				{
					constructor = from.getClass().getDeclaredConstructor(parent.getClass().getSuperclass());
				}
				catch (Exception e)
				{
				}
			}

			if (constructor != null)
			{
				constructor.setAccessible(true);
				result = (T) constructor.newInstance(parent);
			}
			else
			{
				result = (T) from.getClass().newInstance();
			}

			if (result == null)
				return null;

			Class<? extends Object> fromClass = from.getClass();
			Field[] fromFields = fromClass.getDeclaredFields();

			Class<? extends Object> tooClass = result.getClass();
			Field[] tooFields = tooClass.getDeclaredFields();

			if (fromFields != null && tooFields != null)
			{
				for (Field tooF : tooFields)
				{
					try
					{
						Field fromF = fromClass.getDeclaredField(tooF.getName());

						int modifiers = fromF.getModifiers();
						if (!(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)))
						{
							if (fromF.getType().equals(tooF.getType()))
							{
								tooF.setAccessible(true);
								fromF.setAccessible(true);
								Object value = fromF.get(from);

								if (value == null)
								{
									tooF.set(result, value);
								}
								else if (isSimpleField(fromF.getType()))
								{
									tooF.set(result, value);
								}
								else if (IProcess.class.isAssignableFrom(fromF.getType()))
								{
									String xml = ((IProcess) value).getStart().serialize();
									IProcess processT = Start.deserialize(xml);
									tooF.set(result, processT);
								}
								else if (ITexts.class.isAssignableFrom(fromF.getType()))
								{
									ITexts textF = (ITexts) value;
									ITexts it = new Texts();
									for (int langId = 0; langId <= IPhrase.MAX_LANGUAGES; langId++)
									{
										it.setText(langId, textF.getText(langId));
									}
									tooF.set(result, it);
								}
								else if (IPhrase.class.isAssignableFrom(fromF.getType()))
								{
									IPhrase textF = (IPhrase) value;
									IPhrase it = new Phrase();
									String[] langCodes = textF.getLanguageCodes();
									for (String lang : langCodes)
									{
										it.set(lang, textF.get(lang));
									}
									tooF.set(result, it);
								}
								else if (value instanceof Object[])
								{

									Object[] fromArray = (Object[]) value;

									// if (! VasCommand.class.isAssignableFrom(fromF.getType().getComponentType())) {
									tooF.set(result, (Object[]) Array.newInstance(fromF.getType().getComponentType(), fromArray.length));

									for (int i = 0; i < fromArray.length; i++)
									{
										Object toClone = Array.get(fromArray, i);
										Object clonedField = null;
										if (toClone != null)
										{
											if (VasCommand.class.isAssignableFrom(fromF.getType().getComponentType()))
											{
												VasCommand fromVasCommand = (VasCommand) Array.get(fromArray, i);
												clonedField = new VasCommand(fromVasCommand);
											}
											else if (isSimpleField(toClone.getClass()))
											{
												clonedField = Array.get(fromArray, i);
											}
											else
											{
												clonedField = clone(toClone, parent, caller);
											}
											Array.set(tooF.get(result), i, clonedField);
										}
									}
									// }
								}
								else if (value instanceof int[])
								{
									int[] iarr = (int[]) value;
									tooF.set(result, (int[]) Array.newInstance(int.class, iarr.length));
									if (iarr.length > 0)
									{
										for (int i = 0; i < iarr.length; i++)
										{
											Array.set(tooF.get(result), i, iarr[i]);
										}
									}
								}
								else if (fromF.getType().isEnum())
								{
									tooF.set(result, Enum.valueOf(((Class<Enum>) tooF.getType()), value.toString()));
								}
								else
								{
									Object cloned = clone(value, parent, caller);
									tooF.set(result, cloned);
								}
							}
						}
					}
					catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e)
					{
						logger.error("Object Cloning error", e);
					}
				}
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		return result;
	}

	private boolean isSimpleField(Class c)
	{
		return c.isPrimitive() || c == String.class || c == Boolean.class || c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Float.class || c == Double.class
				|| c == Long.class;
	}

	// public boolean isUIHandledArrayType(Class c) {
	// return c.getComponentType().isPrimitive() || c.getComponentType().isEnum() || c.getComponentType() == Texts.class ||
	// c.getComponentType() == String.class || c.getComponentType() == Boolean.class || c.getComponentType() == Byte.class ||
	// c.getComponentType() == Short.class || c == Character.class || c == Integer.class ||
	// c.getComponentType() == Float.class || c.getComponentType() == Double.class
	// || c.getComponentType() == Long.class || c.getComponentType() == Date.class || c.getComponentType() == Calendar.class ||
	// c.getComponentType() == DateRange.class || c.getComponentType() == DateTime.class || c.getComponentType() == TimeUnits.class;
	// }

	public boolean isUIHandledType(Class c)
	{
		return c.isPrimitive() || c.isEnum() || c.equals(Texts.class) || c.equals(Phrase.class) || c.equals(IPhrase.class) || c.equals(java.util.GregorianCalendar.class) || c.equals(String.class)
				|| c.equals(Boolean.class) || c.equals(Byte.class) || c.equals(Short.class) || c.equals(Character.class) || c.equals(Integer.class) || c.equals(Float.class) || c.equals(Double.class)
				|| c.equals(Long.class) || c.equals(java.util.Date.class) || c.equals(Calendar.class) || c.equals(DateRange.class) || c.equals(DateTime.class) || c.equals(TimeUnits.class);
	}

	public String trimToLength(Object o, int length)
	{
		String result = null;
		try
		{
			if (o == null)
			{
				result = "null";
			}
			else if (isSimpleField(o.getClass()))
			{
				result = String.valueOf(o);
			}
			else
			{
				result = o.toString();
			}
		}
		catch (Exception e)
		{
			try
			{
				result = (String) o;
			}
			catch (Exception ex)
			{
				result = o.getClass().toString();
			}
		}

		if (result.length() > length)
		{
			result = result.substring(0, length);
			result += "...";
		}

		return result;
	}

	public ConfigurableResponseParam[] extractObjectStructure(Class cls, IUser user, ISecurity securityService)
	{

		List<ConfigurableResponseParam> parms = new ArrayList<>();
		UiUtils uiutils = new UiUtils();

//		if (cls.equals(ReturnCodeTexts.class))
//		{
//			ConfigurableResponseParam parm = new ConfigurableResponseParam();
//			parm.setFieldName("ReturnCodes");
//			parm.setValueType(cls.getName());
//			parms.add(parm);
//		}
//		else
//		{
			Collection<PropertyInfo> properties = ReflectionHelper.getClassInfo(cls).getProperties().values();
			for (PropertyInfo property : properties)
			{
				String name = property.getName();
				if (name.equals("Version") || name.equals("Configurations") || name.equals("Methods") || name.equals("Properties") || name.equals("Path") || name.equals("Notifications")
						|| name.equals("SerialVersionUID"))
					continue;
				if (property.getGetterMethod() != null)
				{
					ConfigurableResponseParam parm = new ConfigurableResponseParam();
					parm.setFieldName(name);
					Class<?> returnType = property.getGetterMethod().getReturnType();
					parm.setValueType(returnType.getName());

					Method getter = property.getGetterMethod();
					if (getter != null)
					{
						Annotation soanon = getter.getAnnotation(SupplierOnly.class);
						if (soanon == null || securityService.isSupplier(user))
						{
							Annotation anno = getter.getAnnotation(Config.class);
							if (anno != null)
							{
								Config ca = (Config) anno;
								if (ca.hidden())
								{
									continue;
								}
								parm.setDescription(ca.description());
								parm.setComment(ca.comment());
								if (ca.renderAs() != Rendering.DEFAULT)
								{
									parm.setRenderAs(ca.renderAs().toString());
								}
								parm.setMaxLength(ca.maxLength());
								parm.setMinValue(ca.minValue());
								parm.setMaxValue(ca.maxValue());
								parm.setDefaultValue(ca.defaultValue());
								parm.setHidden(ca.hidden());
								parm.setScaleFactor(ca.scaleFactor());
								parm.setDecimalDigitsToDisplay(ca.decimalDigitsToDisplay());
								parm.setGroup(ca.group());
								parm.setUnique(ca.unique());
								parm.setReferencesKey(ca.referencesKey());
							}
						}
					}

					Method setter = property.getSetterMethod();
					String[] enumValues = null;

					if (setter == null)
						parm.setReadOnly(true); // If there is no setter this is also read-only
					else if (setter.getParameterTypes()[0].isEnum())
						enumValues = Arrays.toString(setter.getParameterTypes()[0].getEnumConstants()).substring(1).replaceAll("]$", "").replaceAll(" ", "").split(",");

					if (enumValues != null)
						parm.setPossibleValues(enumValues);

					// is there a structure inside this one?
					if (returnType.isArray() && (!uiutils.isSimpleField(returnType.getComponentType())))
					{
						// Recurse structure into structure
						ConfigurableResponseParam[] structure = extractObjectStructure(returnType.getComponentType(), user, securityService);
						parm.setStructure(structure);
					}

					if (!parm.isHidden())
						parms.add(parm);
				}
			}
//		}
		return parms.toArray(new ConfigurableResponseParam[parms.size()]);
	}

	public ConfigurableResponseParam[] extractParameters(Object obj)
	{
		if (obj == null)
			return null;

		Map<String, PropertyInfo> properties = ReflectionHelper.getClassInfo(obj.getClass()).getProperties();
		Map<String, FieldInfo> fields = ReflectionHelper.getClassInfo(obj.getClass()).getFields();

		ConfigurableResponseParam[] result = new ConfigurableResponseParam[properties.size()];

		int index = 0;
		for (PropertyInfo pi : properties.values())
		{
			result[index] = new ConfigurableResponseParam(pi.getName());

			for (String fieldName : fields.keySet())
			{
				if (fieldName.equalsIgnoreCase(pi.getName()))
				{
					String[] classTypePaths = fields.get(fieldName).getType().toString().split("\\.");
					result[index].setValueType(classTypePaths[classTypePaths.length - 1]);
					if (fields.get(fieldName).getType().isEnum())
					{
						Method setter = pi.getSetterMethod();
						if (setter != null)
						{
							String[] enumValues = Arrays.toString(setter.getParameterTypes()[0].getEnumConstants()).substring(1).replaceAll("]$", "").replaceAll(" ", "").split(",");
							result[index].setPossibleValues(enumValues);
						}
					}
					break;
				}
			}
			index++;
		}
		return result;
	}

	public void populateObjectParameters(Object obj, List<IConfigurableParam> fieldsUpdates)
	{
		if (obj == null)
		{
			return;
		}
		Map<String, PropertyInfo> properties = ReflectionHelper.getClassInfo(obj.getClass()).getProperties();

		for (PropertyInfo pi : properties.values())
		{
			for (IConfigurableParam cp : fieldsUpdates)
			{
				if (cp.getFieldName().equalsIgnoreCase(pi.getName()))
				{
					Method setterMethod = pi.getSetterMethod();
					if (setterMethod != null)
					{
						try
						{
							pi.set(obj, cp.getValue());
						}
						catch (Exception e)
						{
							try
							{
								UiConnectorUtils.setUsingString(obj, String.valueOf(cp.getValue()), setterMethod);
							}
							catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1)
							{
								logger.error("Could not populate parameters", e1);
							}
						}
					}
				}
			}
		}
	}

}
