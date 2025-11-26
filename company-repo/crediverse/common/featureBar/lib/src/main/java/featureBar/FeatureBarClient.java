package featureBar;

import java.io.File;

import java.lang.AutoCloseable;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;

import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureBarClient implements AutoCloseable {
/*
	private static final Logger logger = LoggerFactory.getLogger(FeatureBarClient.class);
	private Client client = null;
	private Watch.Listener listener = null;
	private Watch watch = null;
	private List<Watch.Watcher> watchers = new ArrayList<Watch.Watcher>();
	static private Map<String,Boolean> featureValues = new HashMap<String,Boolean>();
    */

	public FeatureBarClient(
			String etcdEndpoint,
			String privateKeyFilename,
			String certificateFilename, 
			String certificateAuthorityCertificateFilename) {
        /*

		logger.info("Creating a FeatureBarClient!");

		this.listener = Watch.listener(response -> {

			for (WatchEvent event : response.getEvents()) {
				String key = Optional.ofNullable(event.getKeyValue().getKey()).map(bs -> bs.toString(StandardCharsets.UTF_8)).orElse("");
				boolean value = Boolean.parseBoolean(
						Optional.ofNullable(event.getKeyValue().getValue()).map(bs -> bs.toString(StandardCharsets.UTF_8)).orElse("false"));

				logger.info("setting {} to {}", key,value);

				FeatureBarClient.featureValues.put(key,value);
			}
		});

		try {
			File caCert =new File( certificateAuthorityCertificateFilename );
			File keyCertChainFile = new File( certificateFilename );
			File keyFile = new File(privateKeyFilename);

			SslContext context = GrpcSslContexts.forClient()
				.trustManager(caCert)
				.keyManager(keyCertChainFile, keyFile)
				.build();
			
			this.client = Client.builder()
				.endpoints(etcdEndpoint)
				.sslContext(context)
				.build();

			this.watch = client.getWatchClient();
		}
		catch (Exception e) {
			logger.warn("Error: {} ",e.toString());
			this.client = null;
		}
        */
	}

	public void close ()
	{
        /*
		this.watchers.stream().forEach(watcher->watcher.close());
		this.watch.close();
		this.client.close();
		logger.info("Closed a FeatureBarClient");
        */
	}

	public boolean isFeatureAvailable(String featureId) {
        // FeatureBar is currently disabled in production - this means all features are considered enabled by default
        return true;

        /*
		ByteSequence featureIdKey = ByteSequence.from(featureId.getBytes());

		if (client == null) {
			logger.warn("No etcd client connection! All features are considderd off!");
			return false;
		}

		if (featureValues.containsKey(featureId)) {
			return  featureValues.get(featureId);
		} 

		this.watchers.add(watch.watch(featureIdKey, this.listener));

		Boolean available = false;

		KV featuresClient = client.getKVClient();
		CompletableFuture<GetResponse> availableFuture = featuresClient.get(featureIdKey);
		try {
			GetResponse availableResponse = availableFuture.get(500,TimeUnit.MILLISECONDS);

			if (availableResponse.getKvs().isEmpty()) {
				available = false;
				featureValues.put(featureId,available );
			} else {
				String availableString = 
					availableResponse
					.getKvs()
					.get(0)
					.getValue()
					.toString();

				available = Boolean.parseBoolean(availableString);
				featureValues.put(featureId,available);
			}

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.warn("Etcd not responding.  Error: {}", e.toString());
		}
		
		return  available;
        */
	}
} 

