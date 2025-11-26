package cs.config.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import cs.service.AccountService;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter 
{
	@Autowired
	private DefaultTokenServices tokenServices;
	
	@Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception 
	{
        resources.tokenServices(tokenServices);
    }
	
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
        		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        		.headers().frameOptions().disable().and()        		
                .authorizeRequests()                
                .antMatchers(HttpMethod.GET, "/", "/home","/register","/login", "/auth").permitAll()
                .antMatchers(HttpMethod.OPTIONS,"/oauth/token").permitAll()
                .antMatchers(HttpMethod.GET,"/api/account").hasRole("USER")
                .antMatchers(HttpMethod.PUT,"/api/account").authenticated()
                .antMatchers(HttpMethod.GET,"/api/account/balance").authenticated()
                //Bundles
                .antMatchers(HttpMethod.GET,"/api/bundle").authenticated()
                //Logout
                .antMatchers(HttpMethod.POST,"/api/logout").authenticated()
                //Transactions
                .antMatchers(HttpMethod.GET,"/api/account/transaction").authenticated()
                .antMatchers(HttpMethod.POST,"/api/account/transaction/airtime/sale").authenticated()
                .antMatchers(HttpMethod.POST,"/api/account/transaction/transfer").authenticated()
                .antMatchers(HttpMethod.GET,"/api/account/transaction/inbox").authenticated()
                .antMatchers(HttpMethod.PUT,"/api/account/transaction/notify").hasRole("TRUSTED_CLIENT")
                //Users
                .antMatchers(HttpMethod.GET,"/api/account/user").authenticated()
                .antMatchers(HttpMethod.PUT,"/api/account/user").authenticated()
                .antMatchers(HttpMethod.GET,"/api/account/user/password/rules").permitAll()
                .antMatchers(HttpMethod.POST,"/api/account/user/password").authenticated()
                .antMatchers("/api/**").authenticated();
    }
    
    /*
    @Bean
    public JwtAccessTokenConverter accessTokenConverter()  {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.txt");
        logger.info("XXXXXXX Resource 1"); 
        try {
			logger.info("Obtained public key resource: content-length: {}", resource.contentLength());
		} catch (IOException e1) {
			logger.error("IOException {}", e1);
		}
        String publicKey = null;
        try {
        	logger.info("XXXXXXX Resource 2");
            publicKey = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            logger.info("XXXXXXX Resource 3");
        } catch (final IOException e) {
        	logger.error("Exception {}", e);
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        logger.info("XXXXXXX Resource 4");
        converter.setAccessTokenConverter(new JwtConverter());
        logger.info("XXXXXXX Resource 5");
        return converter;
    }
	
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter);
    }
    */
}