package cs.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.ClientState;
import lombok.Getter;

/*
 * This service provides access to the ClientState API on the TS.  It allows the
 * GUI to save state between sessions, but is limited so that the currently logged
 * in user is the only user that can retrieve the stored data.  However, the state
 * can be retrieved in any session by that user.
 */
@Service
public class ClientStateService
{
	private static final Logger logger = LoggerFactory.getLogger(ClientStateService.class);

	private static int CONST_STATE_LENGTH = 9;
	private static int CONST_INDEX_LENGTH = 10;
	private static int CONST_MAX_RETRY = 4;
	private static String CONST_KEY_SEPERATOR = "-";

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ObjectMapper mapper;

	private String restServerUrl;

	/*
	 * Needed to easily get type information
	 * from class at runtime
	 */
	public static class GenericClass<T> {

		private final Class<T> type;

		public GenericClass(Class<T> type) {
			this.type = type;
		}

		public Class<T> getMyType() {
			return this.type;
		}
	}

	@Getter
	public static class ClientStateKey
	{
		protected ApplicationEnum.ClientStateContext ctxt;
		protected String index = "0";

		ClientStateKey()
		{

		}

		ClientStateKey(String value)
		{
			String[] content = value.split(CONST_KEY_SEPERATOR);
			ctxt = ApplicationEnum.ClientStateContext.valueOf(content[0]);
			if (content.length == 2 && content[1].length() > 0)
			{
				index = content[1];
			}
		}

		private ClientStateKey(ApplicationEnum.ClientStateContext value)
		{
			ctxt = value;
		}

		public String toString()
		{
			StringBuilder stringValue = new StringBuilder(ctxt.toString());
			stringValue.append(CONST_KEY_SEPERATOR);
			stringValue.append(String.valueOf(index));
			return stringValue.toString();
		}

		public void setIndex(int index)
		{
			this.index = String.valueOf(index);
		}

		public void setIndex(String index) throws Exception
		{
			if (index.length() > CONST_STATE_LENGTH) throw new Exception("String indexes must be less than "+String.valueOf(CONST_STATE_LENGTH)+" chars, got "+index);
			if (Character.isDigit(index.charAt(0))) throw new Exception("String indexes cannot start with a digit (to avoid conflicts with integer indexes), got "+index);
			this.index = index;
		}
	}

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getClientstateurl();
	}

	private int getNewKey() throws Exception
	{
		int count = CONST_MAX_RETRY;
		while (count > 0)
		{
			try
			{
				ClientState currentValue = restTemplate.execute(restServerUrl+"/INTERNAL-0", HttpMethod.GET, ClientState.class);
				int keyValue = Integer.parseInt(currentValue.getValue());
				keyValue++;
				currentValue.setValue(String.valueOf(keyValue));
				restTemplate.execute(restServerUrl, HttpMethod.PUT, currentValue, Void.class);
				return keyValue;
			}
			catch(Exception ex)
			{
				logger.error("", ex);
			}
		}
		return -1;
	}

	public ClientStateKey generateKey(ApplicationEnum.ClientStateContext context) throws Exception
	{
		if (context.toString().length() > CONST_STATE_LENGTH) throw new Exception("ClientStateContext must be less than "+String.valueOf(CONST_STATE_LENGTH)+" chars, got "+String.valueOf(context));
		ClientStateKey newKey = new ClientStateKey();
		newKey.ctxt = context;
		newKey.setIndex(getNewKey());
		return newKey;
	}

	public ClientStateKey generateKey(ApplicationEnum.ClientStateContext context, int index) throws Exception
	{
		if (index < 0) throw new Exception("ClientStateContext key index must be greater than 0, got "+String.valueOf(index));
		if (context.toString().length() > CONST_STATE_LENGTH) throw new Exception("ClientStateContext must be less than "+String.valueOf(CONST_STATE_LENGTH)+" chars, got "+String.valueOf(context));
		ClientStateKey newKey = new ClientStateKey();
		newKey.ctxt = context;
		newKey.setIndex(index);
		return newKey;
	}

	public <T> ClientStateKey put(ApplicationEnum.ClientStateContext context, T value) throws Exception
	{
		ClientStateKey key = generateKey(context);
		return put(key, value);
	}

	public <T> ClientStateKey put(ApplicationEnum.ClientStateContext context, int index, T value) throws Exception
	{
		ClientStateKey key = new ClientStateKey();
		key.ctxt = context;
		key.setIndex(index);

		return put(key, value);
	}

	public <T> String put(ApplicationEnum.ClientStateContext context, String index, T value) throws Exception
	{
		if (index.length() > CONST_INDEX_LENGTH) throw new Exception("ClientStateContext key with String index cannot be longer than "+String.valueOf(CONST_INDEX_LENGTH)+", got "+String.valueOf(index));
		String key = context.toString()+CONST_KEY_SEPERATOR+index;
		put(key, value);
		return key;
	}

	public <T> ClientStateKey put(ClientStateKey key, T value) throws Exception
	{
		put(key.toString(), value);
		return key;
	}

	private <T> void put(String key, T value) throws Exception
	{
		ClientState stateData = new ClientState();
		stateData.setKey(key);
		stateData.setValue(mapper.writeValueAsString(value));


		ClientState existingValue = get(key, ClientState.class);
		if (existingValue != null)
		{
			stateData.setVersion(existingValue.getVersion());
			restTemplate.execute(restServerUrl, HttpMethod.PUT, stateData, Void.class);
		}
		else
		{
			restTemplate.execute(restServerUrl, HttpMethod.PUT, stateData, Void.class);
		}

	}

	public <T> T get(ApplicationEnum.ClientStateContext context, String index, Class<T> responseType) throws Exception
	{
		String key = context.toString()+CONST_KEY_SEPERATOR+index;
		return get(key, responseType);
	}

	public <T> T get(ApplicationEnum.ClientStateContext context, int index, Class<T> responseType) throws Exception
	{
		ClientStateKey key = new ClientStateKey(context);
		key.setIndex(index);
		return get(key, responseType);
	}

	private <T> T get(String key, Class<T> responseType) throws Exception
	{
		/*
		 * Added as a work around to the TS throwing an internal server error.
		 * FIXME remove this once TS has been fixed.
		 */
		try
		{
			return restTemplate.execute(restServerUrl+"/"+key, HttpMethod.GET, responseType);
		}
		catch(Exception ex){}
		return null;
	}

	public <T> T get(ClientStateKey key, Class<T> responseType) throws Exception
	{
		return get(key.toString(), responseType);
	}
}
