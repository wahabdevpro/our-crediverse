package hxc.utils.protocol.uiconnector.request;

public class GetEcdsTamperResetRequest extends UiBaseRequest
{
	private static final long serialVersionUID = 6473168273548142378L;

	public enum Entity
	{
		ACCOUNT("account"),
		AGENT("agent"),
		AUDITENTRY("auditentry"),
		BATCH("batch");
		
		private final String code;
		
		private Entity(String code) {
	        this.code = code;
	    }
	 
	    public String code() {
	        return code;
	    }
	 
	    public static Entity fromCode(String code) {
	        if (code != null) {
	            for (Entity g : Entity.values()) {
	                if (code.equalsIgnoreCase(g.code)) {
	                    return g;
	                }
	            }
	        }
	        return null;
	    }
	};
	
	private Entity entity;
	private String msisdn;
	
	public Entity getEntity()
	{
		return entity;
	}
	
	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}
	
	
	
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public GetEcdsTamperResetRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
