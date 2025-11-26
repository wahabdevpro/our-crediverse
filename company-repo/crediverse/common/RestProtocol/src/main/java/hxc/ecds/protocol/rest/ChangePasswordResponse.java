package hxc.ecds.protocol.rest;

public class ChangePasswordResponse
{
    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Constants
    //
    // /////////////////////////////////
    public static final String RETURN_CODE_SUCCESS = ResponseHeader.RETURN_CODE_SUCCESS;
    public static final String RETURN_CODE_OK_NOW_REQUIRE_RSA_PASSWORD = "REQUIRE_RSA_PASSWORD";

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Fields
    //
    // ////////////////////////////////
    protected String returnCode;
    protected byte[] key;

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Properties
    //
    // /////////////////////////////////
    public String getReturnCode()
    {
        return returnCode;
    }

    public ChangePasswordResponse setReturnCode(String returnCode)
    {
        this.returnCode = returnCode;
        return this;
    }

    public byte[] getKey()
    {
        return key;
    }

    public ChangePasswordResponse setKey(byte[] key)
    {
        this.key = key;
        return this;
    }
        

}
