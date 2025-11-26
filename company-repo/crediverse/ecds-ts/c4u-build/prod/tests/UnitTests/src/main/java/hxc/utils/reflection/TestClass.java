package hxc.utils.reflection;

import java.util.Date;

public class TestClass extends TestSuperClass
{
	// Fields
	private Date birthday;
	@NonReflective
	private String hidden;

	// Accessors
	public Date getBirthday()
	{
		return birthday;
	}

	public void setBirthday(Date birthday)
	{
		this.birthday = birthday;
	}

	// Decoy get methods
	public void putSurname()
	{
	};

	public void getSurname()
	{
	};

	public String getSurname(String surname)
	{
		return surname;
	}

	public <T> String getFamilyName()
	{
		return "";
	}

	@NonReflective
	public String getHidden()
	{
		return hidden;
	}

	// Decoy set methods
	public int setRank(int age)
	{
		return age;
	}

	public void setRank2()
	{
	}

	@NonReflective
	public void setHidden(String hidden)
	{
		this.hidden = hidden;
	}

}
