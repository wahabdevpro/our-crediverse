package cs.dto.data;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import cs.dto.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ContextData
{
	public enum UserType
	{
		Agent,
		UserAgent,
		WebUser
	}

	private UserType userType;

	private String appName;
	private String appVersion;
	private String githubTag;
	private String branchName;
	private String buildNumber;
	private String buildDateTime;
	private String commitRef;

	private String languageID = null;
	private String countryID = null;
//	private String locale;		// languageID _ countryID
	private User user;
	private int companyID;
	private boolean jsDebug;
	private int timeout;
	private String companyPrefix;
	private int keepalive;
	private String logoFilename;

	// Portal permission (TODO: THIS NEEDS TO BE REMOVED)

	private Map<String, SeperatorSet>seperators;

	public void setUser(User user)
	{
		this.user = user;
	}

	public String getUsername()
	{
		String username = "";
		if (user != null)
		{
			username = user.getUsername();
		}
		return username;
	}

	public void addSeperatorSet(String id, DecimalFormatSymbols symb)
	{
		if (seperators == null)
		{
			seperators = new HashMap<String, SeperatorSet>();
		}
		seperators.put(id, new SeperatorSet(symb));
	}

	@Getter
	@ToString
	public static class SeperatorSet
	{
		private char decimal;
		private char group;
		private char money;

		public SeperatorSet(DecimalFormatSymbols symb)
		{
			decimal = symb.getDecimalSeparator();
			group = symb.getGroupingSeparator();
			money = symb.getMonetaryDecimalSeparator();
		}
	}
}
