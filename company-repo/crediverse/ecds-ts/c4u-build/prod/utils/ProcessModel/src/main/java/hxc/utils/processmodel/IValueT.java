package hxc.utils.processmodel;

public abstract class IValueT<T> extends IValue
{
	@Override
	public abstract T getValue(IProcessState state);

}
