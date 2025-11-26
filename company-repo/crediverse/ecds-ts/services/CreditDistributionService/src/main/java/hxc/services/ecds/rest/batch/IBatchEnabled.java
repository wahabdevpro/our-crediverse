package hxc.services.ecds.rest.batch;

import java.util.Date;

import hxc.ecds.protocol.rest.IValidatable;
import hxc.services.ecds.util.RuleCheckException;

public interface IBatchEnabled<T> extends IValidatable
{
	public abstract int getId();

	public abstract T setId(int id);

	public abstract int getCompanyID();

	public abstract T setCompanyID(int companyID);

	public abstract int getLastUserID();

	public abstract T setLastUserID(int lastUserID);

	public abstract int getVersion();

	public abstract T setVersion(int version);

	public abstract Date getLastTime();

	public abstract T setLastTime(Date lastTime);

	public abstract void validate(T previous) throws RuleCheckException;

}
