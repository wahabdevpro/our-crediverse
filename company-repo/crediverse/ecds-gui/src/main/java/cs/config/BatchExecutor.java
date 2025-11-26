package cs.config;

import cs.service.batch.MsisdnRecycleUploadWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cs.service.batch.BatchProcessingWorker;
import cs.service.batch.BatchUploadWorker;

/*
 * Thread pool used for running batch processing jobs
 * which involves uploading the job to the server and
 * providing status updates during the upload and
 * during processing.
 */
@Configuration
public class BatchExecutor
{
	@Value("${batch.thread.core-pool:10}")
	private int corePoolSize;

	@Value("${batch.thread.max-pool:25}")
	private int maxPoolSize;

	@Value("${batch.queue.capacity:25}")
	private int queueCapacity;

	@Value("${batch.thread.timeout:5}")
	private int threadTimeout;

	@Bean
	@Qualifier("batchExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {

		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setKeepAliveSeconds(threadTimeout);

		return threadPoolTaskExecutor;
	}

	@Bean
	@Scope("prototype")
	public BatchUploadWorker getBatchUploadWorker()
	{
		BatchUploadWorker worker = new BatchUploadWorker();
		return worker;
	}

	@Bean
	@Scope("prototype")
	public BatchProcessingWorker getBatchProcessingWorker()
	{
		BatchProcessingWorker worker = new BatchProcessingWorker();
		return worker;
	}

	@Bean
	@Scope("prototype")
	public MsisdnRecycleUploadWorker getMsisdnRecycleUploadWorker()
	{
		MsisdnRecycleUploadWorker worker = new MsisdnRecycleUploadWorker();
		return worker;
	}
}
