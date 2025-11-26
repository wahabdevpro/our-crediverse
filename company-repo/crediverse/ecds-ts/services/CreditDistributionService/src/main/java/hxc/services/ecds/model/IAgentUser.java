package hxc.services.ecds.model;

import java.util.Date;

import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RuleCheckException;

public interface IAgentUser
{
	public abstract String getLanguage();

	public abstract int getRoleID();

	public abstract boolean isTemporaryPin();

	public abstract String getState();

	public String getImsi();

	public Date getLastImsiChange();

	public abstract void validateAgentImsi(ICreditDistribution context, EntityManager em, //
			TransactionsConfig transactionsConfig, Session session) throws RuleCheckException;

	public abstract byte[] validateNewPin(EntityManager em, CompanyInfo company, String newPin) throws RuleCheckException;

	public abstract String getMobileNumber();

	public boolean testIfSamePin(String pin);

	public abstract String offerPIN(EntityManager em, Session session, CompanyInfo companyInfo, String pin) throws RuleCheckException;

	public abstract void updatePin(EntityManager em, byte[] key, Session session) throws RuleCheckException;

	public abstract String getDomainAccountName();

	public abstract int getId();

	public abstract int getCompanyID();

	public abstract int getAllowedChannels();

	public abstract int getPinVersion();

	public abstract IAgentUser setPinVersion(int pinVersion);

	public abstract void updateAgentImei(ICreditDistribution context, EntityManager em, //
			TransactionsConfig transactionsConfig, Session session) throws RuleCheckException;

	// Location Caching
	public abstract Integer getLastCellID();

	public abstract IAgentUser setLastCellID(Integer lastCellID);

	public abstract Cell getLastCell();

	public abstract IAgentUser setLastCell(Cell lastCell);

	public abstract Date getLastCellExpiryTime();

	public abstract IAgentUser setLastCellExpiryTime(Date lastCellExpiryTime);

	public abstract String getAuthenticationMethod();

}
