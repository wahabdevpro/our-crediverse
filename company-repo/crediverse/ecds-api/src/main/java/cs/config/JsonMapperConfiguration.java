package cs.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JsonMapperConfiguration
{
	@Bean
	@Primary
	public ObjectMapper objectMapperPrimary() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.setSerializationInclusion(Include.NON_EMPTY);

		return mapper;
	}

	@Bean
	@Qualifier("logging")
	public ObjectMapper objectMapperLogging() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		//mapper.setSerializationInclusion(Include.NON_EMPTY);
		return mapper;
	}
}
