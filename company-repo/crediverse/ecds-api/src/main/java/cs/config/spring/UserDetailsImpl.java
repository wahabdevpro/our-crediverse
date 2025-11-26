package cs.config.spring;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 34352346578913L;
	private Integer agentUserID;
	private Integer agentID;
	private Integer webUserID;
	private String username;
	private String password;
	private String sessionId;
	private String agentMsisdn;
	private String agentUserMsisdn;
	private boolean status;	
	/* Unused ?
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	*/
	private List<GrantedAuthority> authorities;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() 
	{
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		
		return status;
	}
}
