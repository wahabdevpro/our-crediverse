package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class UssdConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 3189468015318894954L;
	
	public static final int ROOT_MENU_ID = 1;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected String ussdMenuCommand = "*116#";
	protected List<UssdMenu> menus = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public int getVersion()
	{
		return version;
	}

	public UssdConfig setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getUssdMenuCommand()
	{
		return ussdMenuCommand;
	}

	public UssdConfig setUssdMenuCommand(String ussdMenuCommand)
	{
		this.ussdMenuCommand = ussdMenuCommand;
		return this;
	}

	public List<UssdMenu> getMenus()
	{
		return menus;
	}

	public UssdConfig setMenus(List<UssdMenu> menus)
	{
		this.menus = menus;
		return this;
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConfiguration
	//
	// /////////////////////////////////

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator();
		UssdMenu.validate(validator, menus);
		return validator.toList();

	}

	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	@Override
	public void onPostLoad()
	{

	}

}
