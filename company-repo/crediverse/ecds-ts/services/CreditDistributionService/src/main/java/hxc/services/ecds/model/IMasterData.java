package hxc.services.ecds.model;

import java.util.Date;

import javax.persistence.EntityManager;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RuleCheckException;

public interface IMasterData<T>
{
	public static final int MONEY_PRECISSION = 20;
	public static final int MONEY_SCALE = 4;

	public static final int FINE_MONEY_PRECISSION = 20;
	public static final int FINE_MONEY_SCALE = 8;

	public abstract int getVersion();

	public abstract int getLastUserID();

	public abstract T setLastUserID(int webUserID);

	public abstract Date getLastTime();

	public abstract T setLastTime(Date set);

	public abstract void persist(EntityManager em, T oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException;

	public abstract void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException;

	public abstract void validate(T oldValue) throws RuleCheckException;

}
