package hxc.ui.cli;

public interface ICLIClient
{

	public abstract void setUsername(String username);

	public abstract String getUsername();

	public abstract void setSessionID(String session);

	public abstract String getSessionID();

	public abstract boolean checkForSession();

}
