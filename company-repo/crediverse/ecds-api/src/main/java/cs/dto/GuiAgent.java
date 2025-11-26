package cs.dto;

import cs.dto.enums.LanguageEnum;
import cs.dto.error.GuiValidationException;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.Tier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GuiAgent
{
    final static Logger logger = LoggerFactory.getLogger(GuiAgent.class);
	private int	accountID;
	private String accountNumber;
	private String msisdn;
	private String title;
	private String firstName;
	private String initials;
	private String surname;
	private LanguageEnum language; //enum
	private String altPhoneNumber;
	private String email;
	private String tierName;
	private StateEnum state; //enum
	private String activationDate;
	
	public GuiAgent(Agent agent, Tier tier) throws GuiValidationException
	{
		this.setAccountID(agent.getId());
		
		this.setAccountNumber(agent.getAccountNumber());
		this.setAltPhoneNumber(agent.getAltPhoneNumber());
		if (EmailValidator.getInstance().isValid(agent.getEmail())) {
			this.setEmail(agent.getEmail());
		} else {
			logger.warn(String.format("Email [%s] is not a valid email address", agent.getEmail()));
		}
		this.setFirstName(agent.getFirstName());
		this.setInitials(agent.getInitials());
		this.setLanguage(LanguageEnum.fromString(agent.getLanguage()));
		this.setMsisdn(agent.getMobileNumber());
		this.setState(StateEnum.fromString(agent.getState()));
		this.setSurname(agent.getSurname());
		this.setTierName(tier.getName());
		this.setTitle(agent.getTitle());
		Date activationDate = agent.getActivationDate();
		if(activationDate != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			this.setActivationDate(sdf.format(activationDate));
		}
	}


}
