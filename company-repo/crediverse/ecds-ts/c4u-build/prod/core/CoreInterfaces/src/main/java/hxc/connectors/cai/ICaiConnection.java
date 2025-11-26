package hxc.connectors.cai;

import java.io.IOException;
import java.net.Socket;

import hxc.connectors.IConnection;

public interface ICaiConnection extends IConnection
{
	public abstract Socket getConnection();
	public abstract boolean isLoggedIn();
	public boolean login(String username, String password) throws IOException;
	public String getImei(String msisdn) throws IOException;
}
