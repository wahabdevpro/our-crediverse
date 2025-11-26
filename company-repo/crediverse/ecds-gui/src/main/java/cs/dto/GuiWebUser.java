package cs.dto;

import java.text.SimpleDateFormat;

import org.springframework.beans.BeanUtils;

import cs.dto.users.GuiUserRole;
import hxc.ecds.protocol.rest.WebUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiWebUser extends WebUser implements GuiWebUserView
{

	private String departmentName;
	private GuiUserRole role = null;
	private GuiUserRole[] availableRoles = null;
	private int roleID = 0;

	/*
		{
			*	"id": "3",
			*	"companyID": 2,
		   .
		   .
		   .
			"role": {
				"id": 4,
				"name": "Administration"
			}
		}
	 */

	public GuiWebUser(){}

	public GuiWebUser(WebUser orig)
	{
		BeanUtils.copyProperties(orig, this);
		assignWebUserRole();
	}

	public GuiWebUser(WebUser orig, String departmentName)
	{
		BeanUtils.copyProperties(orig, this);
		this.departmentName = departmentName;
		assignWebUserRole();
	}


	public boolean isSupplier()
	{
		return surname.equals("Supplier") && state.equals("P");
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
			case WebUser.STATE_ACTIVE:
				return "Active";
			case WebUser.STATE_SUSPENDED:
				return "Suspended";
			case WebUser.STATE_DEACTIVATED:
				return "Deactivated";
			case WebUser.STATE_PERMANENT:
				return "Permanent";
			default:
				return "Unknown";
		}
	}

	public void assignWebUserRole()
	{
		if (this.roles != null && this.roles.size() > 0)
		{
			this.roleID = this.getRoles().get(0).getId();
			// Old way
			role = new GuiUserRole(this.getRoles().get(0).getId(), this.getRoles().get(0).getName());
		}
		else
			role = new GuiUserRole(0,  "Not Assigned");
	}

	public String getActDateFormatted()
	{
		if (this.activationDate != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			return sdf.format(this.activationDate);
		} else {
			return "";
		}
	}




}
