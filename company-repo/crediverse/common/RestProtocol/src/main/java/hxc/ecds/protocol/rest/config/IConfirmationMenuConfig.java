package hxc.ecds.protocol.rest.config;

import java.util.List;

public interface IConfirmationMenuConfig extends IConfiguration
{
	public static final String MINS_SINCE_LAST = "{MinutesSinceLast}";
	String RECIPIENT_MSISDN_CONFIRMED = "{RecipientMSISDNConfirmed}";
	String THE_RECIPIENT_NUMBER_AGAIN = "the Recipient's number again";
	String WRONG_B_NUMBER_CONFIRMATION = "MSISDN and RE_MSISDN did not match.";

	public abstract List<UssdMenu> getConfirmationMenus();

	public abstract IConfirmationMenuConfig setConfirmationMenus(List<UssdMenu> confirmationMenus);

	public abstract List<UssdMenu> getDeDuplicationMenus();

	public abstract IConfirmationMenuConfig setDeDuplicationMenus(List<UssdMenu> deDuplicationMenus);

	public abstract boolean isEnableDeDuplication();
	
	public abstract IConfirmationMenuConfig setEnableDeDuplication(boolean enableDeDuplication);

	public abstract int getMaxDuplicateCheckMinutes();
	
	public abstract IConfirmationMenuConfig setMaxDuplicateCheckMinutes(int maxDuplicateCheckMinutes);


}
