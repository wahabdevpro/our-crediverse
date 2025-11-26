package hxc.utils.processmodel;

import java.util.List;

public interface IMenuItem
{
	public abstract int addString(int index, IProcessState state, List<String> menuItems);

	public abstract Action nextActionFor(String input, IProcessState state);

	public abstract Action getSingleActionFor(IProcessState state);

}
