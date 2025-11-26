package hxc.utils.protocol.uiconnector.ctrl.response;

import java.io.Serializable;

public class ComponentFitness implements Serializable
{

	private static final long serialVersionUID = 5367497295709602987L;

	private String name;
	private boolean fit;

	public ComponentFitness()
	{
	}

	public ComponentFitness(String name, boolean fitness)
	{
		this.name = name;
		this.fit = fitness;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the fit
	 */
	public boolean isFit()
	{
		return fit;
	}

	/**
	 * @param fit
	 *            the fit to set
	 */
	public void setFit(boolean fit)
	{
		this.fit = fit;
	}

}
