package cs.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Service;

@Service
public class CorrelationIdService
{
	//private static Logger logger = LoggerFactory.getLogger(CorrelationIdService.class); // used during testing
	private final LongAdder uniqueId = new LongAdder();
	
	private Map<String, Integer>tracker = new  ConcurrentHashMap<String, Integer>();
			
	public String getUniqueId()
	{
		long currentTime = Instant.now().getEpochSecond();
		StringBuffer correlationId = new StringBuffer(Integer.toHexString((int)(currentTime >> 32)));
		correlationId.append(Integer.toHexString((int)currentTime));
		uniqueId.increment();
		correlationId.append(Integer.toHexString(uniqueId.intValue()));
		//logger.info("uniqueID::"+correlationId.toString()); // used during testing
		return correlationId.toString();
	}
	
	public String getTrackingId()
	{
		String newId = getUniqueId();
		int starttime = (int) (System.currentTimeMillis() / 1000L);
		tracker.put(newId, starttime);
		return newId;
	}
	
	public int clearTrackingId(String id)
	{
		return tracker.remove(id);
	}
	
	public boolean trackerComplete(String id)
	{
		return !tracker.containsKey(id);
	}
}
