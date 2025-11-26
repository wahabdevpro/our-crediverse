package hxc.utils.processmodel;

import hxc.utils.processmodel.ui.UIProperties;

public class Test extends Action
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected Action noAction;
	protected Action errorAction;
	private IValueT<Boolean> condition;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public void setNoAction(Action noAction)
	{
		this.noAction = noAction;
	}

	@UIProperties(category = "Flow")
	public Action getNoAction()
	{
		return noAction;
	}

	public void setErrorAction(Action errorAction)
	{
		this.errorAction = errorAction;
	}

	@UIProperties(category = "Flow")
	public Action getErrorAction()
	{
		return errorAction;
	}

	@UIProperties(category = "Input", value = "condition")
	public IValueT<Boolean> getCondition()
	{
		return condition;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Test(Action afterAction, Action errorAction, IValueT<Boolean> condition)
	{
		super(afterAction);
		this.errorAction = errorAction;
		this.condition = condition;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Method
	//
	// /////////////////////////////////
	@Override
	public Action execute(IProcessState state, String command)
	{
		return condition == null || !condition.getValue(state) ? noAction : nextAction;
	}
}
