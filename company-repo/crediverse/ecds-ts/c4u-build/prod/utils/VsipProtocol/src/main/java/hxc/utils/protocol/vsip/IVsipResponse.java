package hxc.utils.protocol.vsip;

public interface IVsipResponse
{
	public abstract int getResponseCode();

	public abstract void setResponseCode(int responseCode);

	public abstract boolean validate(IValidationContext context);
}
