package hxc.ecds.protocol.rest.non_airtime;

import java.math.BigDecimal;

public class DebitRequest extends Request {
    private String agentPin;
    private String originChannel;
    private BigDecimal grossSalesAmount;

    /* Optional GPS Parameters */
    private Double latitude = null;
	private Double longitude = null;
	private Integer gpsAccuracy = null;
	private Integer gpsAge = null;

    public String getAgentPin() {
        return agentPin;
    }

    public void setAgentPin(String agentPin) {
        this.agentPin = agentPin;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(String originChannel) {
        this.originChannel = originChannel;
    }

    public BigDecimal getGrossSalesAmount() {
        return grossSalesAmount;
    }

    public void setGrossSalesAmount(BigDecimal grossSalesAmount) {
        this.grossSalesAmount = grossSalesAmount;
    }

	public Double getLatitude()
	{
		return this.latitude;
	}

	public DebitRequest setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude()
	{
		return this.longitude;
	}

	public DebitRequest setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}

	public Integer getGpsAccuracy()
	{
		return this.gpsAccuracy;
	}

	public DebitRequest setGpsAccuracy(Integer gpsAccuracy)
	{
		this.gpsAccuracy = gpsAccuracy;
		return this;
	}

	public Integer getGpsAge()
	{
		return this.gpsAge;
	}

	public DebitRequest setGpsAge(Integer gpsAge)
	{
		this.gpsAge = gpsAge;
		return this;
	}

}
