package cs.config.spring;

import java.security.KeyPair;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import cs.config.RestServerConfiguration;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

	public static final int OAUTH2_TOKEN_VALIDITY_SECONDS = 29 * 60;

	public static final String RESOURCE_ID = "concurrent.systems.ecds.api";
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private RestServerConfiguration restServerConfig;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception 
	{
		// TODO: Finish ALL THESE ...
		// : Add new database table to store client IDs and secrets and possibly contact details of API client.
		// : Add endpoints to TS maintain client ID and client secret master data
		// : Add functionality to ECDS-GUI to put a front-end on the client ID master data
		// : Implement ClientDetailsService to obtain the client details from the TS.
		clients.inMemory() //Need to implement client details service.
				.withClient("ecdsclient")
				.autoApprove(true)
				.secret("{noop}7a361a9c87824855a9cfba63129730af")
				.authorizedGrantTypes("password", "authorization_code", "refresh_token").scopes("read","write","transact")
				.accessTokenValiditySeconds(OAUTH2_TOKEN_VALIDITY_SECONDS)
			.and()
				.withClient("ecds-ts")
				.autoApprove(true)
				.secret("{noop}3fc3ad76ba19044c43ba498012a6f5b2")
				.authorities("ROLE_TRUSTED_CLIENT")
				.authorizedGrantTypes("client_credentials").scopes("notify");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception 
	{
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
		endpoints
			.tokenStore(tokenStore())
			.authenticationManager(authenticationManager)
			.accessTokenConverter(accessTokenConverter());
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception 
	{
		oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}
	
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() 
    {
        JwtAccessTokenConverter converter = new CustomTokenEnhancer();
        ClassPathResource keyResource = new ClassPathResource("ecds-api.jks");
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(keyResource, restServerConfig.getOAuthKeyStorePassword().toCharArray());
        converter.setAccessTokenConverter(new JwtConverter());
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("ecdsapi", restServerConfig.getOAuthKeyStorePassword().toCharArray());
        converter.setKeyPair(keyPair);
        return converter;
    }

	@Bean
    @Primary 
    public DefaultTokenServices tokenServices()
	{ 
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        //tokenServices.setSupportRefreshToken(true);
        tokenServices.setAuthenticationManager(authenticationManager);
        //tokenServices.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        return tokenServices;
    }
	
	@Bean
	public TokenStore tokenStore() 
	{
		return new JwtTokenStore(accessTokenConverter());
	}
	
    @Bean
    public TokenEnhancer tokenEnhancer() 
    {
        return new CustomTokenEnhancer();
    }
}
