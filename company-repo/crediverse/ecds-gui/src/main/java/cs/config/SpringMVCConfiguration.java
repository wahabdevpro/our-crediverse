package cs.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
@EnableAsync
public class SpringMVCConfiguration
{
	@Bean
	public WebMvcConfigurer corsConfigurer()
	{
		return new WebMvcConfigurer()
		{
			@Override
			public void addCorsMappings(CorsRegistry registry)
			{
				registry.addMapping("/login**")
						.allowedMethods("*")
						.exposedHeaders("X-Login");
				/*
				 * Use this to add cross domain ajax requests.
				 */
				// registry.addMapping("/api/**");
			}

			@Override
			public void addViewControllers(ViewControllerRegistry registry)
			{

			}

			/*@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry)
			{
				// TODO Auto-generated method stub
				addResourceHandlers(registry);

				//registry.addResourceHandler("/plugins/**").addResourceLocations("classpath:/static/adminguilte/plugins");
				//registry.addResourceHandler("/datatables/lang/**").addResourceLocations("classpath:/static/js/lib/datatables/lang");
			}*/
		};
	}

	/*@Bean
	@Primary
	public Jackson2ObjectMapperBuilder getJackson2ObjectMapperBuilder()
	{
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.failOnEmptyBeans(false).serializationInclusion(JsonInclude.Include.NON_EMPTY).indentOutput(false);
		return builder;
	}*/
	
	@Bean
	public LayoutDialect layoutDialect() {
	    return new LayoutDialect();
	}

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

	/*
	 * @Autowired(required = true) public void configeJackson(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) { jackson2ObjectMapperBuilder.serializationInclusion(JsonInclude.Include.
	 * NON_EMPTY); jackson2ObjectMapperBuilder.failOnEmptyBeans(false); }
	 */

	@Bean
	public SpringSecurityDialect getSpringSecurityDialect()
	{
		return new SpringSecurityDialect();
	}

	@Bean
	public AsyncTaskExecutor getAsyncTaskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(10);
		threadPoolTaskExecutor.setMaxPoolSize(200);
		threadPoolTaskExecutor.setQueueCapacity(0);
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		return threadPoolTaskExecutor;
	}
}
