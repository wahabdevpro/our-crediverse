package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class UpdateMyDetailsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 3415392372254186724L;
	private String name;
	private String mobile;

	public UpdateMyDetailsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
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
	 * @return the mobile
	 */
	public String getMobile()
	{
		return mobile;
	}

	/**
	 * @param mobile
	 *            the mobile to set
	 */
	public void setMobile(String mobile)
	{
		this.mobile = mobile;
	}

}
