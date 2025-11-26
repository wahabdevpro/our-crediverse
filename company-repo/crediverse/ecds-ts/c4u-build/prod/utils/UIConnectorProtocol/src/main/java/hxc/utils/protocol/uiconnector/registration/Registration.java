package hxc.utils.protocol.uiconnector.registration;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import hxc.utils.registration.IFacilityRegistration;
import hxc.utils.registration.IRegistration;

public class Registration implements Serializable, IRegistration
{

	private static final long serialVersionUID = 8041610669203163951L;
	private String issuer;
	private Date issueDate;
	private int maxTPS;
	private int maxPeakTPS;
	private int maxNodes;
	private IFacilityRegistration[] facilities;

	@Override
	public String getIssuer()
	{
		return issuer;
	}

	@Override
	public Date getIssueDate()
	{
		return issueDate;
	}

	@Override
	public int getMaxTPS()
	{
		return maxTPS;
	}

	@Override
	public int getMaxPeakTPS()
	{
		return maxPeakTPS;
	}

	@Override
	public int getMaxNodes()
	{
		return maxNodes;
	}

	@Override
	public String getSupplierKey()
	{
		return null;
	}

	@Override
	public IFacilityRegistration[] getFacilities()
	{
		return facilities;
	}

	public Registration(IRegistration registration)
	{
		if (registration == null)
		{
			this.issuer = "Not Assigned";
			return;
		}

		this.issuer = registration.getIssuer();
		this.issueDate = registration.getIssueDate();
		this.maxTPS = registration.getMaxTPS();
		this.maxPeakTPS = registration.getMaxPeakTPS();
		this.maxNodes = registration.getMaxNodes();

		List<FacilityRegistration> facs = new LinkedList<FacilityRegistration>();
		for (IFacilityRegistration fac : registration.getFacilities())
		{
			if (fac == null)
				continue;

			facs.add(new FacilityRegistration(fac));
		}
		this.facilities = facs.toArray(new FacilityRegistration[facs.size()]);
	}

}
