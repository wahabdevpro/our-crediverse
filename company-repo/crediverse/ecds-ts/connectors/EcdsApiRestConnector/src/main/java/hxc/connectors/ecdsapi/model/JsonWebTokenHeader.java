package hxc.connectors.ecdsapi.model;

public class JsonWebTokenHeader {
	String alg;
	String typ;
	
	public String getAlg()
	{
		return alg;
	}
	
	public void setAlg(String alg)
	{
		this.alg = alg;
	}
	
	public String getTyp()
	{
		return typ;
	}
	
	public void setTyp(String typ) 
	{
		this.typ = typ;
	}
}
