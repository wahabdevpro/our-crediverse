package cs.template;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;

public class CsHttpMessageConverterExtractor<T> extends HttpMessageConverterExtractor<T>
{
	private static final Logger logger = LoggerFactory.getLogger(CsHttpMessageConverterExtractor.class);

	public CsHttpMessageConverterExtractor(Class<T> responseType, List<HttpMessageConverter<?>> messageConverters) {
		super(responseType, messageConverters);
	}

	public CsHttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters)
	{
		super(responseType, messageConverters);
	}

	@Override
	public T extractData(ClientHttpResponse response) throws IOException {
		T data = super.extractData(response);

		HttpHeaders headers = response.getHeaders();
		HttpStatus statusCode = response.getStatusCode();
		String statusText = response.getStatusText();

		logger.info("Status Code : "+statusCode.toString());
		logger.info("Status Text : ||"+statusText+"||");
		if (headers.size() > 0)
		{
			logger.info("Headers start");
			for (Entry<String, List<String>> header : headers.entrySet())
			{
				logger.info("Header Name : "+header.getKey());
				for (String headerValue : header.getValue())
				{
					logger.info("Header value ||"+headerValue+"||");
				}
			}
			logger.info("Headers end");
		}

		return data;
	}


}
