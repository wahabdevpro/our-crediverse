package cs.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import cs.template.CsRestTemplate;
@Configuration
public class RestConfiguration
{
	private static Logger logger = LoggerFactory.getLogger(RestConfiguration.class);
	
	@Value("#{servletContext.contextPath}")
	private String contextPath;
	
	@Autowired
	private RestServerConfiguration restServerConfiguration;
	
	private HttpClient createHttpClientInstance()
	{
		// Retrieve TLS configurations
		
		String keyStorePath = restServerConfiguration.getKeystorepath(); 
		String trustStorePath =  restServerConfiguration.getTruststorepath();
		String passwordTrustStore = restServerConfiguration.getTruststorepassword();
		String passwordKeyStore = restServerConfiguration.getKeystorepassword();
		String passwordKey = restServerConfiguration.getKeypassword();
		String protocol = restServerConfiguration.getProtocol();
		int maxConnPerRoute = restServerConfiguration.getMaxConnPerRoute();
		int maxConnTotal = restServerConfiguration.getMaxConnTotal();
	 
		HttpClient httpClient = null;
		//https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e445
		
		// Having files paths set means TLS client is enabled and vice versa
		if (protocol != null && protocol.toLowerCase().equals("http"))
		{
			logger.info("Attempting to connect to transaction server using HTTP");
			logger.info("Set MaxConnTotal: " + maxConnTotal + " and MaxConnPerRoute: " + maxConnPerRoute);
			httpClient = HttpClientBuilder.create()
					.setMaxConnPerRoute(maxConnPerRoute)
					.setMaxConnTotal(maxConnTotal)
					.build();
		}
		else
		{
			if (keyStorePath != null && trustStorePath != null)
			{
				keyStorePath = keyStorePath.trim();
				trustStorePath = trustStorePath.trim();
				if (new File(trustStorePath).canRead())
				{
					logger.error("Cannot read truststorepath "+trustStorePath);
				}
				if (new File(keyStorePath).canRead())
				{
					logger.error("Cannot read keystorepath "+keyStorePath);
				}
			}
			else
			{
				logger.error("SSL/TLS enabled, so truststorepath and keystorepath must be set");
			}
			logger.info("Attempting to connect to transaction server using HTTPS");
			SSLConnectionSocketFactory csf = null;
			try
			{ 
				// Build SSLContext
				SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
						.loadTrustMaterial(new File(trustStorePath), passwordTrustStore.toCharArray())
						.loadKeyMaterial(new File(keyStorePath), passwordKeyStore.toCharArray(), passwordKey.toCharArray())
						.build();
				
				csf = new SSLConnectionSocketFactory(sslContext);
				
				// A http client upon SSL socket
				httpClient = HttpClients.custom()
			                .setSSLSocketFactory(csf)
			                .setMaxConnPerRoute(maxConnPerRoute)
			                .setMaxConnTotal(maxConnTotal)
			                .build();

			}
			catch(java.io.IOException ex)
			{
				
			}
			catch(CertificateException ex)
			{
				httpClient = null;
				logger.error("Certificate validation issue", ex);
			}
			catch(NoSuchAlgorithmException ex)
			{
				httpClient = null;
				logger.error("Requested certificate algorithm is not available", ex);
			}
			catch(KeyManagementException ex)
			{
				httpClient = null;
				logger.error("General key management issue, please verify that the correct keys are being used.", ex);
			}
			catch(KeyStoreException ex)
			{
				httpClient = null;
				logger.error("Keystore exception, please verify that the keystore exists and is not corrupt", ex);
			}
			catch(UnrecoverableKeyException ex)
			{
				httpClient = null;
				logger.error("Unable to recover the key from the keystore, please verify that the keystore exists and is not corrupt", ex);
			}
			catch(Exception ex)
			{
				httpClient = null;
				logger.error("SSL/TLS connection to transaction server failed", ex);
			}
		}
		
		return httpClient;
	}
	
	private ClientHttpRequestFactory clientHttpRequestFactory() throws Exception
	{
		HttpClient httpClient = createHttpClientInstance();
		
	    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
	    if (factory != null)
	    {
		    factory.setReadTimeout(restServerConfiguration.getTimeout());
		    factory.setConnectTimeout(restServerConfiguration.getTimeout());
	    }
	    return factory;
	}
	
	@Bean
	public RestTemplate getRestTemplate() throws Exception
	{
		RestTemplate bean = null;
		ClientHttpRequestFactory factory = clientHttpRequestFactory();
		if (factory != null)
		{
			bean = new RestTemplate(factory);
			bean.getMessageConverters()
			        .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		}
		else
		{
			throw new Exception("Failed to create HTTP factory");
		}
		return bean;
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CsRestTemplate getCsRestTemplate()
	{
		return new CsRestTemplate();
	}

	@Bean
	public CsServletConfiguration getCsServletConfiguration()
	{
		CsServletConfiguration config = new CsServletConfiguration();
		config.setServletContextPath(contextPath);
		return config;
	}
}
