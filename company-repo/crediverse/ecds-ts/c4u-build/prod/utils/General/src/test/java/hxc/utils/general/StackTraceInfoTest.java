package hxc.utils.general;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTraceInfoTest {
	final static Logger logger = LoggerFactory.getLogger(StackTraceInfoTest.class);

	@Test
	public void testConstruction() throws Exception {
		int counter = 0;
		logger.info("[counter={}]====================================", counter++);
		{
			StackTraceInfo stackTraceInfo = StackTraceInfo.here();
			logger.info("[counter={}]stackTraceInfo.summaryAll() = {}", counter, stackTraceInfo.summaryAll(null, 0, null, false));
			logger.info("[counter={}]stackTraceInfo.summary() = {}", counter, stackTraceInfo.summary(null, 0));
			logger.info("[counter={}]stackTraceInfo.invariantSummary() = {}", counter, stackTraceInfo.invariantSummary(null, 0));
			assertEquals( stackTraceInfo.invariantSummary(null, 0), "hxc.utils.general.StackTraceInfoTest:testConstruction" );
		}
		logger.info("[counter={}]====================================", counter++);
		{
			StackTraceInfo stackTraceInfo = new StackTraceInfo( Thread.currentThread().getStackTrace(), 1 );
			logger.info("[counter={}]stackTraceInfo.summaryAll() = {}", counter, stackTraceInfo.summaryAll(null, 0, null, false));
			logger.info("[counter={}]stackTraceInfo.summary() = {}", counter, stackTraceInfo.summary(null, 0));
			logger.info("[counter={}]stackTraceInfo.invariantSummary() = {}", counter, stackTraceInfo.invariantSummary(null, 0));
			assertEquals( stackTraceInfo.invariantSummary(null, 0), "hxc.utils.general.StackTraceInfoTest:testConstruction" );
		}
	}

	@Test
	public void testSummaries() throws Exception {
		int counter = 0;
		logger.info("[counter={}]====================================", counter++);
		{
			StackTraceInfo stackTraceInfo = StackTraceInfo.here();
			logger.info("[counter={}]stackTraceInfo.summaryAll() = {}", counter, stackTraceInfo.summaryAll(null, 0, 5, true));
		}
		{
			StackTraceInfo stackTraceInfo = StackTraceInfo.here(1);
			logger.info("[counter={}]stackTraceInfo.summaryAll() = {}", counter, stackTraceInfo.summaryAll(null, 0, 5, true));
		}
		{
			String string = new StackTraceInfo.Summariser(StackTraceInfo.here(1), null, 0, 5, true).toString();
			logger.info("[counter={}]stackTraceInfo.summaryAll() = {}", counter, string);
		}
	}
}
