package hxc.services.ecds.model;

import javax.persistence.EntityManager;

import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RuleCheckException;

public interface IAuthenticatable extends hxc.ecds.protocol.rest.IAuthenticatable
{
	public abstract byte[] validateNewPin(EntityManager em, CompanyInfo company, String newPin) throws RuleCheckException;

	public abstract String offerPIN(EntityManager em, Session session, CompanyInfo companyInfo, String pin) throws RuleCheckException;

	public abstract void updatePin(EntityManager em, byte[] key, Session session) throws RuleCheckException;
	
	public abstract byte[] getKey1();

	public abstract IAuthenticatable setKey1(byte[] key1);
	
	public abstract byte[] getKey2();

	public abstract IAuthenticatable setKey2(byte[] key2);

	public abstract byte[] getKey3();

	public abstract IAuthenticatable setKey3(byte[] key3);

	public abstract byte[] getKey4();

	public abstract IAuthenticatable setKey4(byte[] key4);
	
	public abstract Integer getConsecutiveAuthFailures();

	public abstract IAuthenticatable setConsecutiveAuthFailures(Integer consecutiveAuthFailures);
	
}
