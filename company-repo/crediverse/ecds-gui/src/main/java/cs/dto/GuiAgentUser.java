package cs.dto;

import java.text.SimpleDateFormat;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiAgentUser extends AgentUser /*implements GuiAgentUserView*/
{
	private Role role = null;

	private boolean channelUssd;
	private boolean channelSms;
	private boolean channelApi;
	private boolean channelWui;
	private boolean channelApp;

	private boolean channelUssdAvailable;
	private boolean channelSmsAvailable;
	private boolean channelApiAvailable;
	private boolean channelWuiAvailable;
	private boolean channelAppAvailable;

	public GuiAgentUser(){}

	public GuiAgentUser(AgentUser orig)
	{
		BeanUtils.copyProperties(orig, this);



		// Current Agent User Channel location
		int channels = getAllowedChannels();
		if ( (channels & AgentUser.ALLOWED_USSD) > 0 ) channelUssd = true;
		if ( (channels & AgentUser.ALLOWED_SMS) > 0 ) channelSms = true;
		if ( (channels & AgentUser.ALLOWED_API) > 0 ) channelApi = true;
		if ( (channels & AgentUser.ALLOWED_WUI) > 0 ) channelWui = true;
		if ( (channels & AgentUser.ALLOWED_APP) > 0 ) channelApp = true;
	}

	public void updateAvailableChannels(Agent agent)
	{
		// First find out what can be done
		int availableChannels = agent.getAllowedChannels();
		if ( (availableChannels & Agent.ALLOWED_USSD) > 0 ) channelUssdAvailable = true;
		if ( (availableChannels & Agent.ALLOWED_SMS) > 0 ) channelSmsAvailable = true;
		if ( (availableChannels & Agent.ALLOWED_API) > 0 ) channelApiAvailable = true;
		if ( (availableChannels & Agent.ALLOWED_WUI) > 0 ) channelWuiAvailable = true;
		if ( (availableChannels & Agent.ALLOWED_APP) > 0 ) channelAppAvailable = true;
	}

	public String getFullName()
	{
		if ((firstName != null) && (surname != null))
			return String.format("%s %s", firstName, surname);
		else if (firstName != null)
			return firstName;
		else
			return surname;
	}

	public String getStateDescription()
	{
		if (state == null)
			return "Unknown";

		switch(state) {
			case AgentUser.STATE_ACTIVE:
				return "Active";
			case AgentUser.STATE_DEACTIVATED:
				return "Deactivated";
			default:
				return "Unknown";
		}
	}

	public String getActivationDateFormatted()
	{
		if (this.activationDate != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			return sdf.format(this.activationDate);
		}
		return "";
	}

	public AgentUser extractAgentUser()
	{
		int channels = 0;
		if(channelUssd) channels |= Agent.ALLOWED_USSD;
		if(channelSms) channels |= Agent.ALLOWED_SMS;
		if(channelApi) channels |= Agent.ALLOWED_API;
		if(channelWui) channels |= Agent.ALLOWED_WUI;
		if(channelApp) channels |= Agent.ALLOWED_APP;
		setAllowedChannels(channels);

		AgentUser agentUser = new AgentUser();
		BeanUtils.copyProperties(this, agentUser);
		return agentUser;
	}

}
