package cs.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class EcdsAuthenticationToken extends AbstractAuthenticationToken
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1808816714050926534L;
	private Object principal;
	private Object credentials;

	/**
	 * This constructor can be safely used by any code that wishes to create a <code>UsernamePasswordAuthenticationToken</code>, as the {@link #isAuthenticated()} will return <code>false</code>.
	 *
	 */
	public EcdsAuthenticationToken(Object principal, Object credentials)
	{
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(false);
	}

	/**
	 * This constructor should only be used by <code>AuthenticationManager</code> or <code>AuthenticationProvider</code> implementations that are satisfied with producing a trusted (i.e.
	 * {@link #isAuthenticated()} = <code>true</code>) authentication token.
	 *
	 * @param principal
	 * @param credentials
	 * @param authorities
	 */
	public EcdsAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities)
	{
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		super.setAuthenticated(true); // must use super, as we override
	}

	@Override
	public Object getCredentials()
	{
		return credentials;
	}

	@Override
	public Object getPrincipal()
	{
		return principal;
	}

	@Override
	public void eraseCredentials()
	{
		super.eraseCredentials();
		credentials = null;
	}
}
