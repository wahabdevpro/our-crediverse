package hxc.ui.cli.interpreter.elements.parent;

import hxc.ui.cli.ICLIClient;
import hxc.ui.cli.connector.ICLIConnector;
import hxc.ui.cli.system.ICLISystem;

public interface IElement
{

	public abstract void setConnector(ICLIConnector connector);

	public abstract void setSystem(ICLISystem system);

	public abstract void setClient(ICLIClient client);

	public abstract String regex();

	public abstract boolean matches(String comparison);

	public abstract boolean action(String action);

	public abstract String getError();

}
