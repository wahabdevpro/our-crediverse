package hxc.services;

import hxc.connectors.IConnector;

public interface IAction
{
	public <T> void processResponse(T request, IConnector connector);
}
