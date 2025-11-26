package cs.dto;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import cs.dto.GuiTier.TierType;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentAccount;
import hxc.ecds.protocol.rest.AgentAccountEx;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiAgentAccount extends Agent
{
	private TierType tierType;
	private String tierTypeCode;

	private boolean channelUssd;
	private boolean channelSms;
	private boolean channelApi;
	private boolean channelWui;
	private boolean channelApp;

	protected String tierName;
	protected String groupName;
	protected String serviceClassName;
	protected String supplierName;
	protected String ownerName;
	protected String areaName;
	protected String areaType;
	protected String roleName;

	protected StateEnum currentState;
	protected BigDecimal balance;
	protected BigDecimal bonusBalance;
	protected BigDecimal onHoldBalance;
	protected boolean accountTamperedWith;

	private enum StateEnum {
		ACTIVE(STATE_ACTIVE),
		SUSPENDED(STATE_SUSPENDED),
		DEACTIVATED(STATE_DEACTIVATED),
		PERMANENT(STATE_PERMANENT);
		private String val;
		private StateEnum(String val) {
			this.val = val.toUpperCase();
		}

		// Getters can be used by the variables that use StateEnum
		@SuppressWarnings("unused")
		public String getVal(String val)
		{
			return this.val;
		}

		public static StateEnum fromString(String val)
		{
			StateEnum result = DEACTIVATED;
			if (val != null)
			{
				switch (val)
				{
					case STATE_ACTIVE:
						result = ACTIVE;
						break;
					case STATE_SUSPENDED:
						result = SUSPENDED;
						break;
					case STATE_DEACTIVATED:
						result = DEACTIVATED;
						break;
					case STATE_PERMANENT:
						result = PERMANENT;
						break;
				}
			}
			return result;
		}
	}

	public GuiAgentAccount()
	{
		super();
	}

	public GuiAgentAccount(Agent orig)
	{
		BeanUtils.copyProperties(orig, this);

		int channels = getAllowedChannels();
		if ( (channels & Agent.ALLOWED_USSD) > 0 ) channelUssd = true;
		if ( (channels & Agent.ALLOWED_SMS) > 0 ) channelSms = true;
		if ( (channels & Agent.ALLOWED_API) > 0 ) channelApi = true;
		if ( (channels & Agent.ALLOWED_WUI) > 0 ) channelWui = true;
		if ( (channels & Agent.ALLOWED_APP) > 0 ) channelApp = true;

		currentState = StateEnum.fromString(getState());
	}

	public GuiAgentAccount(AgentAccount orig)
	{
		BeanUtils.copyProperties(orig.getAgent(), this);

		int channels = getAllowedChannels();
		if ( (channels & Agent.ALLOWED_USSD) > 0 ) channelUssd = true;
		if ( (channels & Agent.ALLOWED_SMS) > 0 ) channelSms = true;
		if ( (channels & Agent.ALLOWED_API) > 0 ) channelApi = true;
		if ( (channels & Agent.ALLOWED_WUI) > 0 ) channelWui = true;
		if ( (channels & Agent.ALLOWED_APP) > 0 ) channelApp = true;

		currentState = StateEnum.fromString(getState());
	}

	public GuiAgentAccount(AgentAccountEx orig)
	{
		BeanUtils.copyProperties(orig, this);

		int channels = getAllowedChannels();
		if ( (channels & Agent.ALLOWED_USSD) > 0 ) channelUssd = true;
		if ( (channels & Agent.ALLOWED_SMS) > 0 ) channelSms = true;
		if ( (channels & Agent.ALLOWED_API) > 0 ) channelApi = true;
		if ( (channels & Agent.ALLOWED_WUI) > 0 ) channelWui = true;
		if ( (channels & Agent.ALLOWED_APP) > 0 ) channelApp = true;

		currentState = StateEnum.fromString(getState());

		tierType = TierType.getTierType(orig.getTierType());
		tierTypeCode = orig.getTierType();
		supplierName = orig.getSupplierFirstName() + " " + orig.getSupplierSurname();
		ownerName = orig.getOwnerFirstName() + " " + orig.getOwnerSurname();
	}

	public Agent getAgent()
	{
		int channels = 0;
		if(channelUssd) channels |= Agent.ALLOWED_USSD;
		if(channelSms) channels |= Agent.ALLOWED_SMS;
		if(channelApi) channels |= Agent.ALLOWED_API;
		if(channelWui) channels |= Agent.ALLOWED_WUI;
		if(channelApp) channels |= Agent.ALLOWED_APP;
		setAllowedChannels(channels);

		Agent agent = new Agent();
		BeanUtils.copyProperties(this, agent);
		return agent;
	}

	public GuiAgentAccount setAccountTamperedWith(boolean tamperedWith)
	{
		accountTamperedWith = tamperedWith;
		return this;
	}
}
