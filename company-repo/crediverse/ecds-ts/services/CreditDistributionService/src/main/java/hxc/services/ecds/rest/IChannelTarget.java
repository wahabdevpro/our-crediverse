package hxc.services.ecds.rest;

import java.util.Map;

import javax.persistence.EntityManager;

import hxc.connectors.IInteraction;
import hxc.services.ecds.CompanyInfo;

public interface IChannelTarget
{
	public abstract void defineChannelFilters(EntityManager em, ICreditDistribution context, CompanyInfo company);

	public abstract boolean processChannelRequest(int companyID, IInteraction interaction, Map<String, String> values, int tag);
}
