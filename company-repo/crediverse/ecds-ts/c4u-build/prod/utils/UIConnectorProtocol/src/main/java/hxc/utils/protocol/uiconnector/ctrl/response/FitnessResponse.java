package hxc.utils.protocol.uiconnector.ctrl.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class FitnessResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 995691125617567933L;
	private ComponentFitness components[];
	private ComponentFitness services[];

	public FitnessResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the components
	 */
	public ComponentFitness[] getComponents()
	{
		return components;
	}

	/**
	 * @param components
	 *            the components to set
	 */
	public void setComponents(ComponentFitness[] components)
	{
		this.components = components;
	}

	/**
	 * @return the services
	 */
	public ComponentFitness[] getServices()
	{
		return services;
	}

	/**
	 * @param services
	 *            the services to set
	 */
	public void setServices(ComponentFitness[] services)
	{
		this.services = services;
	}

}
