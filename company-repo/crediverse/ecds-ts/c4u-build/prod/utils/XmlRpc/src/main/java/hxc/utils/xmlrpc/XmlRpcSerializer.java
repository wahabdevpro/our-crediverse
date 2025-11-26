/**
 *
 */
package hxc.utils.xmlrpc;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import hxc.utils.calendar.ISO8601DateFormat;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;
import hxc.utils.string.StringUtils;

/**
 * @author AndriesdB
 * 
 */
public class XmlRpcSerializer
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private final ISO8601DateFormat iso8601Format = new ISO8601DateFormat();
	// private final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");
	private SAXParser saxParser;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Token Literals
	//
	// /////////////////////////////////
	private static final int T_TEXT = 0;
	private static final int T_ARRAY = 1;
	private static final int T_BASE64 = 2;
	private static final int T_BOOLEAN = 3;
	private static final int T_DATA = 4;
	private static final int T_DATETIME = 5;
	private static final int T_DOUBLE = 6;
	private static final int T_FAULT = 7;
	private static final int T_INT = 8;
	private static final int T_MEMBER = 9;
	private static final int T_METHODCALL = 10;
	private static final int T_METHODNAME = 11;
	private static final int T_METHODRESPONSE = 12;
	private static final int T_NAME = 13;
	private static final int T_PARAM = 14;
	private static final int T_PARAMS = 15;
	private static final int T_STRING = 16;
	private static final int T_STRUCT = 17;
	private static final int T_VALUE = 18;
	private static final int T_ROOT = 19;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	/**
	 * 
	 * @param args
	 * 
	 */
	public XmlRpcSerializer()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Serialisation
	//
	// /////////////////////////////////

	public String serialize(Object graph)
	{
		// Null Case
		if (graph == null)
			return null;

		// Result
		// TODO Tune initial size optimally
		StringBuilder result = new StringBuilder(1000);
		result.append("<?xml version=\"1.0\"?>\n");

		// Get Fields
		ClassInfo classInfo = ReflectionHelper.getClassInfo(graph.getClass());
		XmlRpcMethod methodAnnotation = classInfo.getAnnotation(XmlRpcMethod.class);

		if (graph instanceof XmlRpcException)
		{
			// TODO

		}
		else if (methodAnnotation != null)
		{
			result.append("<methodCall>\n");
			result.append(String.format("<methodName>%s</methodName>\n", methodAnnotation.name()));
			createParams(graph, classInfo.getFields().values(), result);
			result.append("</methodCall>\n");
		}
		else
		{
			result.append("<methodResponse>\n");
			createParams(graph, classInfo.getFields().values(), result);
			result.append("</methodResponse>\n");
		}

		return result.toString();
	}

	private void createParams(Object graph, Collection<FieldInfo> collection, StringBuilder result)
	{
		result.append("<params>\n");
		for (FieldInfo field : collection)
		{
			result.append("<param>\n");
			Object value;
			try
			{
				value = field.get(graph);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				value = null;
			}
			appendValue(field, field.getType(), field.getName(), value, result);
			result.append("</param>\n");
		}
		result.append("</params>\n");
	}

	private void appendValue(FieldInfo field, Class<?> type, String name, Object value, StringBuilder result)
	{
		// AsString
		boolean asString = field.isAnnotationPresent(XmlRpcAsString.class);

		// Format
		XmlRpcFormat format = field.getAnnotation(XmlRpcFormat.class);

		// Start value element
		result.append("<value>\n");

		// Scalars
		if (!type.isArray())
		{
			// Force to string
			if (asString)
			{
				String stringValue = String.format("<string>%s</string>\n", value == null ? "" : value.toString());
				result.append(stringValue);
			}
			else if (type.isEnum())
			{
				String intValue = value == null ? "" : String.format("%d", ((Enum<?>) value).ordinal());
				intValue = String.format("<i4>%s</i4>\n", intValue);
				result.append(intValue);
			}
			else
			{
				// Switch on Type name
				switch (type.getName())
				{

				// DateTime
					case "java.util.Date":
					{
						DateFormat dateFormat = format == null ? iso8601Format : new SimpleDateFormat(format.format());
						String dateValue = String.format("<dateTime.iso8601>%s</dateTime.iso8601>\n", value == null ? "" : dateFormat.format(value));
						result.append(dateValue);
					}
						break;

					// String
					case "java.lang.String":
					{
						String stringValue = String.format("<string>%s</string>\n", value == null ? "" : StringUtils.escapeXml((String) value));
						result.append(stringValue);
					}
						break;

					// Boolean
					case "java.lang.Boolean":
					case "boolean":
					{
						String boolValue = value == null ? "" : (boolean) value ? "1" : "0";
						boolValue = String.format("<boolean>%s</boolean>\n", boolValue);
						result.append(boolValue);
					}
						break;

					// Integer
					case "java.lang.Integer":
					case "int":
					{
						String intValue = value == null ? "" : value.toString();
						intValue = String.format("<i4>%s</i4>\n", intValue);
						result.append(intValue);
					}
						break;

					// Double
					case "java.lang.Double":
					case "double":
					{
						String doubleValue = value == null ? "" : value.toString();
						doubleValue = String.format("<double>%s</double>\n", doubleValue);
						result.append(doubleValue);
					}
						break;

					// Else assume it is a structure
					default:
						appendStruct(field, type, name, value, result);
				}
			}

		}

		// Byte Arrays
		else if (type.getComponentType().getName() == "byte" || type.getComponentType().getName() == "java.lang.Byte")
		{
			String base64Value = String.format("<base64>%s</base64>\n", value == null ? "" : DatatypeConverter.printBase64Binary((byte[]) value));
			result.append(base64Value);
		}

		// Other Arrays
		else
		{
			appendArray(field, type, name, value, result);
		}

		// End value element
		result.append("</value>\n");

	}

	private void appendStruct(FieldInfo field, Class<?> type, String name, Object value, StringBuilder result)
	{
		// Start structure element
		result.append("<struct>\n");

		if (value != null)
		{
			// Get Fields
			ClassInfo structInfo = ReflectionHelper.getClassInfo(type);
			Collection<FieldInfo> fields = structInfo.getFields().values();

			// For each Field
			for (FieldInfo member : fields)
			{
				// Get value
				Object memberValue;
				try
				{
					memberValue = member.get(value);
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					memberValue = null;
				}
				if (memberValue == null)
					continue;

				// Create member node
				result.append("<member>\n");

				// Create name node
				result.append("<name>" + member.getName() + "</name>");

				// Append Value
				appendValue(member, member.getType(), member.getName(), memberValue, result);

				// End member node
				result.append("</member>\n");
			}
		}

		// End structure element
		result.append("</struct>\n");

	}

	private void appendArray(FieldInfo field, Class<?> type, String name, Object value, StringBuilder result)
	{

		// Create array and data elements
		result.append("<array>\n<data>\n");

		// Iterate over them
		if (value != null)
		{
			int length = java.lang.reflect.Array.getLength(value);
			for (int index = 0; index < length; index++)
			{
				appendValue(field, type.getComponentType(), "", java.lang.reflect.Array.get(value, index), result);
			}
		}

		// End data and array elements
		result.append("</data>\n</array>\n");

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SAX Style De-Serialisation
	//
	// /////////////////////////////////
	@SuppressWarnings("unchecked")
	public <T> T deSerialize(InputStream stream, Class<T> type) throws XmlRpcException
	{
		return (T) deSerializeAny(stream, type);
	}

	public synchronized Object deSerializeAny(InputStream stream, Class<?>... types) throws XmlRpcException
	{
		// Construct a Map of Methods
		final Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>();
		for (Class<?> type : types)
		{
			ClassInfo classInfo = ReflectionHelper.getClassInfo(type);
			Annotation annotation = classInfo.getAnnotation(XmlRpcMethod.class);
			if (annotation != null)
			{
				XmlRpcMethod xmethod = (XmlRpcMethod) annotation;
				typeMap.put(xmethod.name(), type);
			}
			else
				typeMap.put("RESPONSE", type);
		}

		// Create a parse stack
		final Stack<ParseState> stack = new Stack<ParseState>();
		stack.push(new ParseState(T_ROOT, stack));

		try
		{
			// Create SAX Parser if we don't have one yet
			if (saxParser == null)
			{
				// Create a SAX Parser Factory
				SAXParserFactory factory = SAXParserFactory.newInstance();

				// Create a SAX Parser
				saxParser = factory.newSAXParser();
			}

			// Create handler
			DefaultHandler handler = new DefaultHandler()
			{
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					parse(tokenFromName(qName), null);
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException
				{
					parse(-tokenFromName(qName), null);
				}

				private int tokenFromName(String qName) throws SAXException
				{
					switch (qName)
					{
						case "array":
							return T_ARRAY;
						case "base64":
							return T_BASE64;
						case "boolean":
							return T_BOOLEAN;
						case "data":
							return T_DATA;
						case "dateTime.iso8601":
							return T_DATETIME;
						case "double":
							return T_DOUBLE;
						case "fault":
							return T_FAULT;
						case "i4":
							return T_INT;
						case "int":
							return T_INT;
						case "member":
							return T_MEMBER;
						case "methodCall":
							return T_METHODCALL;
						case "methodName":
							return T_METHODNAME;
						case "methodResponse":
							return T_METHODRESPONSE;
						case "name":
							return T_NAME;
						case "param":
							return T_PARAM;
						case "params":
							return T_PARAMS;
						case "string":
							return T_STRING;
						case "struct":
							return T_STRUCT;
						case "value":
							return T_VALUE;
						default:
							throw new SAXException(qName);
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException
				{
					String text = new String(ch, start, length);
					parse(T_TEXT, text);
				}

				// Parse
				private void parse(int token, String text) throws SAXException
				{
					ParseState state = stack.get(stack.size() - 1);
					if (token == T_TEXT)
					{
						if (state.text == null)
							state.text = text;
						else
							state.text += text;

						return;
					}

					switch (state.state)
					{
					// Root
						case T_ROOT:
							if (state.canPush(token, T_METHODCALL))
							{
							}
							else if (state.canPush(token, T_METHODRESPONSE))
							{
							}
							else if (state.mustPop(token))
							{
							}
							break;

						case T_METHODCALL:
							if (state.step == 0)
							{
								state.expects(token, T_METHODNAME);
								state.text = null;
							}
							else if (state.step == 1)
							{
								state.expects(token, -T_METHODNAME);

								// Create Instance
								Class<?> type = typeMap.get(state.text);
								if (type == null)
									throw new SAXException("Unknown MethodCall: " + state.text);
								state.classInfo = new ClassInfo(type);
								try
								{
									state.parent.instance = state.instance = state.classInfo.newInstance();
								}
								catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
								{
									throw new SAXException(e.getMessage());
								}
							}
							else if (state.step == 2)
							{
								state.mustPush(token, T_PARAMS);
							}
							else if (state.mustPop(token))
							{

							}
							break;

						case T_METHODRESPONSE:
							if (state.step == 0)
							{
								if (token == T_FAULT)
								{
									state.parent.instance = state.instance = new XmlRpcException("XML-RPC Fault");
									state.mustPush(token, T_FAULT);
								}
								else
								{
									// Create Instance
									Class<?> type = typeMap.get("RESPONSE");
									if (type == null)
										throw new SAXException("Unknown MethodCall: " + state.text);
									state.classInfo = new ClassInfo(type);
									try
									{
										state.parent.instance = state.instance = state.classInfo.newInstance();
									}
									catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
									{
										throw new SAXException(e.getMessage());
									}

									state.mustPush(token, T_PARAMS);
								}
							}
							else if (state.mustPop(token))
							{

							}
							break;

						case T_PARAMS:
							if (token == T_PARAM)
							{
								FieldInfo fieldInfo = state.classInfo.getField(state.step);
								state.mustPush(token, T_PARAM).fieldInfo = fieldInfo;
							}
							else if (state.mustPop(token))
							{
							}
							break;

						case T_PARAM:
							if (state.step == 0)
							{
								state.mustPush(token, T_VALUE);
							}
							else if (state.mustPop(token))
							{
							}
							break;

						case T_VALUE:
							if (state.step != 0)
							{
								state.mustPop(token);
							}
							else if (token == T_STRUCT)
							{
								ParseState newState = state.mustPush(token, T_STRUCT);
								if (!newState.isFault)
								{
									newState.classInfo = new ClassInfo(state.getBaseType());
									try
									{
										newState.instance = newState.classInfo.newInstance();
									}
									catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
									{
										throw new SAXException(e.getMessage(), e);
									}
								}
							}
							else if (state.canPush(token, T_ARRAY))
							{
							}
							else if (state.canPush(token, T_STRING))
							{
							}
							else if (state.canPush(token, T_BASE64))
							{
							}
							else if (state.canPush(token, T_BOOLEAN))
							{
							}
							else if (state.canPush(token, T_DATETIME))
							{
							}
							else if (state.canPush(token, T_DOUBLE))
							{
							}
							else if (state.canPush(token, T_INT))
							{
							}
							else
								state.error(token);
							break;

						case T_STRING:
							if (state.mustPop(token))
							{
								if (state.isFault)
								{
									((XmlRpcException) state.instance).setMessage(state.text);
								}
								else
								{
									Class<?> type = state.getBaseType();
									if (type == String.class)
										state.setValue(state.text == null ? "" : state.text);
									else if (!state.hasText())
									{
									}
									else if (type == Long.class || type == long.class)
									{
										state.setValue(Long.parseLong(state.text));
									}
									else if (type == Boolean.class || type == boolean.class)
									{
										state.setValue(Boolean.parseBoolean(state.text));
									}
									else if (type.isEnum())
									{
										try
										{
											Method valueOf = type.getDeclaredMethod("valueOf", String.class);
											state.setValue(valueOf.invoke(null, state.text));
										}
										catch (Exception e)
										{
											throw new SAXException(e.getMessage(), e);
										}

									}
								}

							}
							break;

						case T_BASE64:
							if (state.mustPop(token))
							{
								if (state.hasText())
									state.setValue(DatatypeConverter.parseBase64Binary(state.text));
							}
							break;

						case T_BOOLEAN:
							if (state.mustPop(token))
							{
								if (state.hasText())
									state.setValue(state.text.equals("1") ? true : false);
							}
							break;

						case T_DATETIME:
							if (state.mustPop(token))
							{
								if (state.hasText())
								{
									try
									{
										XmlRpcFormat format = null;

										if (state.fieldInfo != null)
											format = (XmlRpcFormat) state.fieldInfo.getAnnotation(XmlRpcFormat.class);

										if (format == null)
										{
											state.setValue((Date) iso8601Format.parse(state.text));
										}
										else
										{
											SimpleDateFormat special = new SimpleDateFormat(format.format());
											state.setValue((Date) special.parse(state.text));
										}
									}
									catch (ParseException e)
									{
										throw new SAXException(e.getMessage(), e);
									}
								}
							}
							break;

						case T_DOUBLE:
							if (state.mustPop(token))
							{
								if (state.hasText())
									state.setValue(Double.parseDouble(state.text));
							}
							break;

						case T_INT:
							if (state.mustPop(token))
							{
								if (state.hasText())
								{

									int value = 0;

									value = Integer.parseInt(state.text);

									if (state.isFault)
									{
										state.setValue(value);
									}
									else
									{
										Class<?> type = state.getBaseType();

										if (type.isEnum())
										{
											state.setValue(type.getEnumConstants()[value]);
										}
										else
											state.setValue(value);
									}
								}
							}
							break;

						case T_STRUCT:
							if (state.canPush(token, T_MEMBER))
							{
							}
							else if (state.mustPop(token))
							{
								state.setValue(state.instance);
							}
							break;

						case T_MEMBER:
							if (state.step == 0)
							{
								state.expects(token, T_NAME);
								state.text = null;
							}
							else if (state.step == 1)
							{
								state.expects(token, -T_NAME);
								if (!state.isFault)
									state.fieldInfo = state.classInfo.getFields().get(state.text);
							}
							else if (state.step == 2)
							{
								state.mustPush(token, T_VALUE);
							}
							else if (state.mustPop(token))
							{
							}
							break;

						case T_ARRAY:
							if (state.step == 0)
							{
								ParseState newState = state.mustPush(token, T_DATA);
								newState.arrayList = new ArrayList<Object>();
							}
							else if (state.mustPop(token))
							{

							}
							break;

						case T_DATA:
							if (token == T_VALUE)
							{
								state.arrayList.add(null);
								ParseState newState = state.mustPush(token, T_VALUE);
								newState.isElement = true;
							}
							else if (state.mustPop(token))
							{
								int count = state.arrayList.size();
								Object array = Array.newInstance(state.getBaseType(), count);
								for (int index = 0; index < count; index++)
								{
									Array.set(array, index, state.arrayList.get(index));
								}
								state.setValue(array);
							}
							break;

						case T_FAULT:
							if (state.step == 0)
							{
								state.isFault = true;
								state.mustPush(token, T_VALUE);
							}
							else if (state.mustPop(token))
							{
							}
							break;

						default:
							state.error(token);
					}
				}

			};

			// Parse
			saxParser.parse(stream, handler);

			// Return Result
			if (stack.size() != 1)
				throw new SAXException("Malformed XML");

			Object result = stack.get(0).instance;

			if (result instanceof XmlRpcException)
				throw (XmlRpcException) result;

			return result;

		}
		catch (XmlRpcException e)
		{
			throw e;
		} catch (SAXException e) {
			int nonDeterministicError = 999;
			throw new XmlRpcException(e.getMessage(), nonDeterministicError, e);
		} catch (Exception e)
		{
			throw new XmlRpcException(e.getMessage(), 0, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SAX Style De-Serialisation Helper Class
	//
	// /////////////////////////////////

	private class ParseState
	{

		private boolean isFault = false;
		private Stack<ParseState> stack;
		private int state;
		private int step;
		private String text = null;
		@SuppressWarnings("unused")
		private String name;
		private Object instance;
		private ClassInfo classInfo;
		private FieldInfo fieldInfo;
		@SuppressWarnings("unused")
		private Object element;
		private ArrayList<Object> arrayList;
		private ParseState parent;
		private boolean isElement;

		private ParseState(int state, Stack<ParseState> stack)
		{
			this.state = state;
			this.stack = stack;
		}

		private ParseState(int state, ParseState parent)
		{
			this.parent = parent;
			this.state = state;
			this.stack = parent.stack;
			this.instance = parent.instance;
			this.classInfo = parent.classInfo;
			this.fieldInfo = parent.fieldInfo;
			this.isFault = parent.isFault;
		}

		private boolean mustPop(int token) throws SAXException
		{
			if (state == -token)
			{
				stack.pop();
				return true;
			}
			else
			{
				error(token);
				return false;
			}
		}

		private boolean canPush(int token, int expected)
		{
			if (token != expected)
				return false;
			stack.push(new ParseState(token, this));
			step++;
			return true;
		}

		private boolean error(int token) throws SAXException
		{
			String error = String.format("Invalid Token %d in State %d Step %d", token, state, step);
			throw new SAXException(error);
		}

		private void expects(int token, int expected) throws SAXException
		{
			if (token != expected)
				error(token);
			step++;
		}

		private ParseState mustPush(int token, int expected) throws SAXException
		{
			if (token != expected)
				error(token);
			ParseState newState = new ParseState(token, this);
			stack.push(newState);
			step++;
			return newState;
		}

		private void setValue(Object value) throws SAXException
		{

			try
			{
				if (isFault)
				{
					if (value instanceof XmlRpcException)
						parent.instance = value;
					else
						((XmlRpcException) parent.instance).setErrorCode(value);

				}
				else if (!parent.isElement)
				{
					fieldInfo.set(parent.instance, value);
				}
				else
				{
					ArrayList<Object> arrayList = parent.parent.arrayList;
					arrayList.set(arrayList.size() - 1, value);
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new SAXException(e.getMessage(), e);
			}
		}

		private boolean hasText()
		{
			return text != null && text.length() > 0;
		}

		private Class<?> getBaseType()
		{
			if (isArray())
				return fieldInfo.getField().getType().getComponentType();
			else
				return fieldInfo.getField().getType();
		}

		private boolean isArray()
		{
			return fieldInfo.getField().getType().isArray();
		}

	}

}