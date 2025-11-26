package hxc.utils.protocol.uiconnector.request;

public class GetEcdsTamperCheckRequest extends UiBaseRequest
{
	private static final long serialVersionUID = 4700037773378870237L;

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
	
	public Entity getEntity()
	{
		return entity;
	}
	
	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}
	
	public GetEcdsTamperCheckRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
