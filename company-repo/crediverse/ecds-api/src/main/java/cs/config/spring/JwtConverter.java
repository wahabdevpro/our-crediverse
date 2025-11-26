package cs.config.spring;

import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;


public class JwtConverter extends DefaultAccessTokenConverter implements Converter<OAuth2ResourceServerProperties.Jwt, AbstractAuthenticationToken> {

    public void configure(JwtAccessTokenConverter converter) {
        converter.setAccessTokenConverter(this);
    }

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication auth = super.extractAuthentication(map);
        auth.setDetails(map); //this will get spring to copy JWT content into Authentication
        return auth;
    }

    @Override
    public AbstractAuthenticationToken convert(OAuth2ResourceServerProperties.Jwt jwt) {
        return null;
    }
}
