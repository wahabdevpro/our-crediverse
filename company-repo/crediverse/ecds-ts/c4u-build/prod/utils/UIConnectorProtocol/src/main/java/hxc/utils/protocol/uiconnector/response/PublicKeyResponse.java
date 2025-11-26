package hxc.utils.protocol.uiconnector.response;

public class PublicKeyResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -7956487887143534506L;
	private byte[] publicKey;

	public PublicKeyResponse()
	{
	}

	public PublicKeyResponse(String userId)
	{
		super(userId);
		setResponseCode(UiResponseCode.PUBLIC_KEY);
	}

	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey()
	{
		return publicKey;
	}

	/**
	 * @param publicKey
	 *            the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey)
	{
		this.publicKey = publicKey;
	}

}
