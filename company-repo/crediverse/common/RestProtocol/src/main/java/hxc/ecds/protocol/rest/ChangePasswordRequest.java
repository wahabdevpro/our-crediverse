package hxc.ecds.protocol.rest;

//REST End-Point: ~/web_users/change_password
public class ChangePasswordRequest
{
    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Constants
    //
    // /////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Fields
    //
    // /////////////////////////////////
    protected Integer entityID;
    //protected byte[] data;//TODO This is security by obscurity; change this to String password.
    protected String newPassword;
    protected String currentPassword;

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Properties
    //
    // /////////////////////////////////
    public Integer getEntityID()
    {
        return entityID;
    }

    public void setEntityID(Integer entityID)
    {
        this.entityID = entityID;
    }

    /*public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }*/

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
    
    public String getCurrentPassword()
    {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword)
    {
        this.currentPassword = currentPassword;
    }
}
