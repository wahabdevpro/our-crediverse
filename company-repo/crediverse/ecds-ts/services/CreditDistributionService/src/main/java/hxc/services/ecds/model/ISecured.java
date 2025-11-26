package hxc.services.ecds.model;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

public interface ISecured<T>
{
	public abstract long getSignature();

	public abstract T setSignature(long signature);

	@Transient
	public abstract boolean isTamperedWith();

	public abstract T setTamperedWith(boolean tamperedWith);

	public abstract long calcSecuritySignature();

	@PreUpdate
	@PrePersist
	public abstract void onPrePersist();

	@PostLoad
	public abstract void onPostLoad();

}
