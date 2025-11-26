package cs.dto;

import java.util.ArrayList;

import org.apache.commons.validator.routines.EmailValidator;

import cs.dto.GuiTransaction.TransactionTypeEnum;
import cs.dto.enums.LanguageEnum;
import cs.dto.error.GuiValidationException;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.Violation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GuiUser
{
	private int userID;
	private String title;
	private String firstName;
	private String surname;
	private String initials;
	private LanguageEnum language;
	private String email;
	private String mobileNumber;
	private StateEnum state;

	public GuiUser(AgentUser agentUser)
	{
		this.userID = agentUser.getAgentID();
		this.title = agentUser.getTitle();
		this.firstName = agentUser.getFirstName();
		this.surname = agentUser.getSurname();
		this.initials = agentUser.getInitials();
		this.language = LanguageEnum.fromString(agentUser.getLanguage());
		this.email = agentUser.getEmail();
		this.mobileNumber = agentUser.getMobileNumber();
		this.state = StateEnum.fromString(agentUser.getState());
	}
	
	public ArrayList<Violation> validate() throws GuiValidationException
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		if(title == null || title.isEmpty())
			violations.add(new Violation(Violation.INVALID_VALUE, "title", null, "Field name title cannot be empty."));
		
		if(firstName == null || firstName.isEmpty())
			violations.add(new Violation(Violation.INVALID_VALUE, "firstName", null, "Field name firstName cannot be empty."));
		
		if(surname == null || surname.isEmpty())
			violations.add(new Violation(Violation.INVALID_VALUE, "surname", null, "Field name surname cannot be empty."));
		
		if(initials == null || initials.isEmpty())
			violations.add(new Violation(Violation.INVALID_VALUE, "initials", null, "Field name initials cannot be empty."));

		if(email == null || email.isEmpty())
		{
			violations.add(new Violation(Violation.INVALID_VALUE, "email", null, "Field name email cannot be empty."));
		}
		else if( !EmailValidator.getInstance().isValid(email))
		{
			String error = String.format("Email address %s is not valid.", this.getEmail());
			violations.add(new Violation(Violation.INVALID_VALUE, "email", null, error));
		}
		if(mobileNumber == null || mobileNumber.isEmpty())
			violations.add(new Violation(Violation.INVALID_VALUE, "mobileNumber", null, "Field name mobileNumber cannot be empty."));
		return violations;
	}
	
	public enum StateEnum {
		ACTIVE(Agent.STATE_ACTIVE),
		SUSPENDED(Agent.STATE_SUSPENDED),
		DEACTIVATED(Agent.STATE_DEACTIVATED),
		PERMANENT(Agent.STATE_PERMANENT);
		private String val;
		private StateEnum(String val) 
		{
			this.val = val.toUpperCase();
		}
		
		public String getVal()
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
					case Agent.STATE_ACTIVE:
						result = ACTIVE;
						break;
					case Agent.STATE_SUSPENDED:
						result = SUSPENDED;
						break;
					case Agent.STATE_DEACTIVATED:
						result = DEACTIVATED;
						break;
					case Agent.STATE_PERMANENT:
						result = PERMANENT;
						break;
				}
			}
			return result;
		}
	
		public static boolean contains(String value)
		{
			for (TransactionTypeEnum c : TransactionTypeEnum.values()) {
		        if (c.name().equals(value)) {
		            return true;
		        }
		    }
		    return false;
		}
	}
}
