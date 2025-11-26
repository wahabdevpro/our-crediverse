package hxc.utils.protocol.uiconnector.request;

public class PublicKeyRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -1919522007804819369L;

	public PublicKeyRequest(String userId)
	{
		super(userId);
		init();
	}

	private void init()
	{
		setRequestCode(UiRequestCode.PUBLIC_KEY);
	}
}
