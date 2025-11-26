package hxc.services.ecds;

import java.util.Arrays;
import java.util.List;

public class AuditEntryContext {
	private String key;
	private List<Object> attributes;
	private boolean skipAuditLog = false;
		
	public AuditEntryContext(String key, Object... args)
	{
		this.key = key;
		attributes = Arrays.asList(args);
	}
	public String getReason() {
		return key;
	}
	public void setReason(String reason) {
		this.key = reason;
	}
	public List<Object> getAttributes() {
		return attributes;
	}
	public void setAttributes(Object... args) {
		attributes = Arrays.asList(args);
	}
	public boolean isSkipAuditLog() {
		return skipAuditLog;
	}
	public void setSkipAuditLog(boolean val) {
		this.skipAuditLog = val;
	}
}
