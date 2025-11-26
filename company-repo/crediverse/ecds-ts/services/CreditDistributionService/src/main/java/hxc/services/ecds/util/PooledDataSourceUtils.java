package hxc.services.ecds.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.PooledDataSource;

public class PooledDataSourceUtils {
	final static Logger logger = LoggerFactory.getLogger(PooledDataSourceUtils.class);

	private final PooledDataSource pooledDataSource;

	//private final static ConnectionSummary connectionSummaryInfo;

	public PooledDataSourceUtils(PooledDataSource pooledDataSource) {
		this.pooledDataSource = pooledDataSource;
	}

	public static String summariseConnections(PooledDataSource pooledDataSource) {
		String error = null;
		int numConnections = -1;
		int numIdleConnections = -1;
		int numBusyConnections = -1;
		int numUnclosedOrphanedConnections = -1;
		try {
			numBusyConnections = pooledDataSource.getNumBusyConnections();
			numIdleConnections = pooledDataSource.getNumIdleConnections();
			numConnections = pooledDataSource.getNumConnections();
			numUnclosedOrphanedConnections = pooledDataSource.getNumUnclosedOrphanedConnections();
		}
		catch ( Throwable throwable ) {
			logger.warn("summariseConnections: caught", throwable);
			error = throwable.getMessage();
		}
		return String.format("numConnections = %s, numIdleConnections = %s, numBusyConnections = %s, numUnclosedOrphanedConnections = %s%s%s",
			numConnections, numIdleConnections, numBusyConnections, numUnclosedOrphanedConnections,
			(error == null ? "" : "ERROR: "), (error == null ? "" : error));
	}

	public String summariseConnections() {
		return summariseConnections(this.pooledDataSource);
	}

	public static class ConnectionSummariser {
		private final PooledDataSource pooledDataSource;
		public ConnectionSummariser(PooledDataSource pooledDataSource) {
			this.pooledDataSource = pooledDataSource;
		}
		@Override
		public String toString() {
			return summariseConnections(pooledDataSource);
		}
	}

	public static ConnectionSummariser connectionSummariser( PooledDataSource pooledDataSource ) {
		return new ConnectionSummariser(pooledDataSource);
	}
}
