package cs.config.spring;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomTokenEnhancer extends JwtAccessTokenConverter 
{
	private static final Logger logger = LoggerFactory.getLogger(CustomTokenEnhancer.class);

	@Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) 
	{
		Map<String, Object> additionalInfo = new HashMap<String, Object>();
        Object auth = authentication.getPrincipal();
        if(auth instanceof UserDetailsImpl )
        {
        	UserDetailsImpl userDetails = (UserDetailsImpl)auth;
        	additionalInfo.put("sid", userDetails.getSessionId());
        	additionalInfo.put("agentUserID", userDetails.getAgentUserID());
        	additionalInfo.put("agentID", userDetails.getAgentID());
        	additionalInfo.put("webUserID", userDetails.getWebUserID());
        	additionalInfo.put("agentMsisdn", userDetails.getAgentMsisdn());
        	additionalInfo.put("agentUserMsisdn", userDetails.getAgentUserMsisdn());
            DefaultOAuth2AccessToken defaultAccessToken = (DefaultOAuth2AccessToken)accessToken;
        	defaultAccessToken.setAdditionalInformation(additionalInfo);
        } else if(authentication.isClientOnly()) {
            logger.warn("authentication.isClientOnly() reached. We don't handle this authentication path");
       	} else {
       		throw new OAuth2Exception("Unable to enhance OAUTH2 with ECDS Transaction server session ID");
       	}
        DefaultOAuth2AccessToken customAccessToken = new DefaultOAuth2AccessToken(accessToken);
        return super.enhance(customAccessToken, authentication);
    }
}