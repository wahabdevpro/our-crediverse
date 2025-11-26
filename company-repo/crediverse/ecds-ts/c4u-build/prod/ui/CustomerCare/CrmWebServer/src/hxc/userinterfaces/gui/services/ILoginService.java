package hxc.userinterfaces.gui.services;

import hxc.userinterfaces.gui.data.User;

public interface ILoginService
{
	public User login(String userName, String password);

	public String getVersion();
}
