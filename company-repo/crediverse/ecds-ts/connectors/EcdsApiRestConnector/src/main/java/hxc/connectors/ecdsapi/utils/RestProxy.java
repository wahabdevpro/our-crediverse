package hxc.connectors.ecdsapi.utils;

import javax.net.ssl.SSLContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
//import hxc.services.ecds.CreditDistribution;

public class RestProxy implements AutoCloseable
{
	protected Client restClient;
	protected String baseURL;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	final String TRUSTORE_CLIENT_FILE = "truststore_client";
	final String TRUSTSTORE_CLIENT_PWD = "123456";
	final String KEYSTORE_CLIENT_FILE = "keystore_client";
	final String KEYSTORE_CLIENT_PWD = "123456";
	
	protected ClientConfig configuration = new ClientConfig();
	
	protected static final int CONNECTION_TIMOUT_DEFAULT = 3000;
	protected static final int READ_TIMOUT_DEFAULT = 3000;
	
	protected int connectionTimeout;
	protected int readTimeout;

	public enum ConnectionType
	{
		NO_TLS, TLS, TLS_CLIENT_AUTH
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public RestProxy()
	{
		configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMOUT_DEFAULT);
		configuration = configuration.property(ClientProperties.READ_TIMEOUT, READ_TIMOUT_DEFAULT);
	}

	public RestProxy(String uri, int connectionTimeout, int readTimeout, boolean enableTLS)
	{
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
		configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
		configuration = configuration.property(ClientProperties.READ_TIMEOUT, readTimeout);
		buildClient(uri, connectionTimeout, readTimeout, (enableTLS ? ConnectionType.TLS_CLIENT_AUTH : ConnectionType.NO_TLS));
	}

	public void buildClient(String uri, int connectionTimeout, int readTimeout, ConnectionType type)
	{
		restClient = null;
		// Try to create rest Client with/without TLS enabled
		switch (type)
		{
			case TLS_CLIENT_AUTH:
				restClient = newClientTLSwithAuth(connectionTimeout, readTimeout);
				break;
			case TLS:
				restClient = newClientTLSnoAuth(connectionTimeout, readTimeout);
				break;
			case NO_TLS:
				break;
			default:
				break;
		}

		if (restClient == null)
		{
			// Not HTTPS connection
            //ObjectMapper mapper = new ObjectMapper();
            //mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

			JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();
			ObjectMapper objectMapper = jacksonJsonProvider.locateMapper(Object.class, MediaType.APPLICATION_JSON_TYPE);
            objectMapper.registerModule(new JSR310Module());
			restClient = ClientBuilder.newClient(configuration).register(jacksonJsonProvider);
		}
		baseURL = UriBuilder.fromUri(uri).build().toString();
		// Enable Jackson JSON representation
		restClient.register(new JacksonFeature());
	}

	private Client newClientTLSwithAuth(int connectionTimeout, int readTimeout)
	{
		// Load client keyStore and trustStore files
		//java.net.URL keyStorePath = CreditDistribution.class.getClassLoader().getResource(KEYSTORE_CLIENT_FILE);
		//java.net.URL trustStorePath = CreditDistribution.class.getClassLoader().getResource(TRUSTORE_CLIENT_FILE);
		
		// Martin Cooper testing to break our circulsr dependency.
		java.net.URL keyStorePath = this.getClass().getClassLoader().getResource(KEYSTORE_CLIENT_FILE);
		java.net.URL trustStorePath = this.getClass().getClassLoader().getResource(TRUSTORE_CLIENT_FILE);

		if (keyStorePath == null || trustStorePath == null)
			return null;

		SslConfigurator sslConfig = SslConfigurator.newInstance().trustStoreFile(trustStorePath.getPath()).trustStorePassword(TRUSTSTORE_CLIENT_PWD).keyStoreFile(keyStorePath.getPath())
				.keyPassword(KEYSTORE_CLIENT_PWD);

		final SSLContext sslContext = sslConfig.createSSLContext();

		// create rest Client with SSL context
		return ClientBuilder.newBuilder().sslContext(sslContext).withConfig(configuration).build();

	}

	// SSL Connection without client Authentication provided
	private Client newClientTLSnoAuth(int connectionTimeout, int readTimeout)
	{
		// Martin Cooper testing to break our circulsr dependency.
		//java.net.URL trustStorePath = CreditDistribution.class.getClassLoader().getResource(TRUSTORE_CLIENT_FILE);
		java.net.URL trustStorePath = this.getClass().getClassLoader().getResource(TRUSTORE_CLIENT_FILE);
		
		if (trustStorePath == null)
			return null;
		SslConfigurator sslConfig = SslConfigurator.newInstance().trustStoreFile(trustStorePath.getPath()).trustStorePassword(TRUSTSTORE_CLIENT_PWD);
		Client client = ClientBuilder.newBuilder().sslContext(sslConfig.createSSLContext()).withConfig(configuration).build();
		return client;
	}

	@Override
	public void close() throws Exception {
		this.restClient.close();
	}

	public Builder builder(String path)
	{
		UriBuilder uriBuilder = UriBuilder.fromUri(baseURL).path(path);
		String fullPath = uriBuilder.build().toString();
		WebTarget target = restClient.target(fullPath);
		Builder result = target.request().accept(MediaType.APPLICATION_JSON);
		return result;
	}

	public boolean isOK(Response result)
	{
		return result.getStatus() == 200 || result.getStatus() == 204;
	}

	public void put(String path, String accessToken, Object entity)
	{
		Response response = builder(path).header("Authorization", "Bearer " + accessToken)
				.put(Entity.json(entity));
		checkOK(response);
	}

	private void checkOK(Response response)
	{
		int status = response.getStatus();
		if (status < 200 || status >= 300)
			throw new WebApplicationException(status);
	}
	
	public <T> T postClientCredentialsAuth(String path, String clientID, String clientSecret, Class<T> cls)
	{
		String rawAuth = String.format("%s:%s", clientID, clientSecret);
		String encodedAuth = Base64.encodeBase64String(rawAuth.getBytes());
		String authHeader = String.format("Basic %s", encodedAuth);
		MultivaluedMap<String,String> formData = new MultivaluedHashMap<String,String>();
		formData.add("grant_type", "client_credentials");
		return builder(path).header("Authorization", authHeader).post(Entity.form(formData), cls);
	}
}
