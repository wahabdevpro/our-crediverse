package cs.service;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;

@Service
public abstract class GenericService<T> extends Exportable
{
	private Class<?> genericClassType;			// e.g. Bundle.class
	private Class<?> genericClassArrayType;		// e.g. Bundle[].class

	@Autowired
	protected RestServerConfiguration restServerConfig;

	@Autowired
	protected CsRestTemplate restTemplate;

	private boolean configured = false;
	private String restServerUrl;

	protected void loadConfigurion(String serviceSpecificUrl)
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + serviceSpecificUrl;

			this.genericClassType = getGenericTypeClass();
			this.genericClassArrayType = getGenericTypeClassArray();

			configured = true;
		}
	}

	@SuppressWarnings("unchecked")
	private Class<T> getGenericTypeClass() {
		try {
			String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
			Class<?> clazz = Class.forName(className);
			return (Class<T>) clazz;
		} catch (Exception e) {
			throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
		}
	}

	// http://stackoverflow.com/questions/4901128/obtaining-the-array-class-of-a-component-type
	private Class<?> getGenericTypeClassArray() {
		try
		{
			return Class.forName("[L" + this.getGenericTypeClass().getName() + ";");
		}
		catch(Exception e) {
			return Array.newInstance(getGenericTypeClass().getComponentType(), 0).getClass();
		}
	}

	public T retrieve(int id) throws Exception
	{
		@SuppressWarnings("unchecked")
		T response = (T) restTemplate.execute(String.format("%s/%s", restServerUrl, id), HttpMethod.GET, genericClassType);
		return response;
	}

	public T retrieve(String id) throws Exception
	{
		int iid = ((id ==null) || (id.length()==0))? 0 : Integer.parseInt(id);
		return retrieve(iid);
	}

	@SuppressWarnings("unchecked")
	public T[] list(String filter, String search, int offset, int limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);

		RestRequestUtil.standardSearch(uri, search);

		RestRequestUtil.standardPaging(uri, offset, limit);

		RestRequestUtil.standardSorting(uri, sort);

		return  (T[]) restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, genericClassArrayType);

	}

	public T[] list(String search, int offset, int limit) throws Exception
	{
		return list(null, search, offset, limit, null);
	}

	public T[] list(int offset, int limit) throws Exception
	{
		return list(null, null, offset, limit, null);
	}

	public T[] list(String search) throws Exception
	{
		return list(null, search, -1, -1, null);
	}

	public T[] list(Map<String, String> params) throws Exception
	{
		if (params.containsKey("start") && params.containsKey("length"))
		{
			int start = Integer.parseInt(params.get("start"));
			int length = Integer.parseInt(params.get("length"));
			return list(start, length);
		}
		else
		{
			return list();
		}
	}

	public T[] list() throws Exception
	{
		return list(null, null, -1, -1, null);
	}

	public void create(T newObject) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newObject, Void.class);
	}

	public void update(T updatedObject) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedObject, Void.class);
	}

	public void delete(String idToDelete) throws Exception
	{
		restTemplate.execute(String.format("%s/%s", restServerUrl, idToDelete), HttpMethod.DELETE, Void.class);
	}

	/**
	 * Assumes that the DropDown is Populated by int:String pairs
	 * @param type
	 * @param query
	 * @param keyField		Name of key (Integer) field, e.g. "id"
	 * @param valueField	Name of value (String) field e.g. "name"
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, String> getDropDownMap(Optional<String> type, Optional<String> query, String keyField, String valueField) throws Exception
	{
		Map<Integer, String>classMap = new TreeMap<Integer,String>();
		T[] list = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			list = list(query.get());
		}
		else
		{
			list = list();
		}

		if (list != null)
		{
			Arrays.asList(list).forEach(item ->{

				BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(item);
				Integer key = (Integer) wrapper.getPropertyValue(keyField);
				String value = (String) wrapper.getPropertyValue(valueField);

				classMap.put(key, value);

			});
		}

		return classMap;
	}

	public Long count(String filter, String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");

		RestRequestUtil.standardFilter(uri, filter);

		RestRequestUtil.standardSearch(uri, search);

		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public Long count(String search) throws Exception
	{
		return count(null, search);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");

		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);

		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

}
