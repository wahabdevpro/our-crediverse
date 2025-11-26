package za.co.concurrent.NetworkIdentifier;
import java.util.Date;


public class NetID {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + ((ipAdress == null) ? 0 : ipAdress.hashCode());
		result = prime * result + ((lastUpdated == null) ? 0 : lastUpdated.hashCode());
		result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetID other = (NetID) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.contains(other.hostname)&&!other.hostname.contains(hostname))
			return false;
		if (ipAdress == null) {
			if (other.ipAdress != null)
				return false;
		} else if (!ipAdress.equals(other.ipAdress))
			return false;
		if (macAddress == null) {
			if (other.macAddress != null)
				return false;
		} else if (!macAddress.equals(other.macAddress))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "NetPrint [macAddress=" + macAddress + ", ipAdress=" + ipAdress + ", hostname=" + hostname
				+ ", lastUpdated=" + lastUpdated + "]";
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getIpAdress() {
		return ipAdress;
	}
	public void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public NetID(String macAddress, String ipAdress, String hostname, Date lastUpdated) {
		super();
		this.macAddress = macAddress;
		this.ipAdress = ipAdress;
		this.hostname = hostname;
		this.lastUpdated = lastUpdated;
	}
	public NetID(String macAddress, String ipAdress, String hostname ) {
		super();
		this.macAddress = macAddress;
		this.ipAdress = ipAdress;
		this.hostname = hostname;
		this.lastUpdated = new Date();
	}
	protected String macAddress;
	protected String ipAdress;
	protected String hostname;
	protected Date lastUpdated;
}