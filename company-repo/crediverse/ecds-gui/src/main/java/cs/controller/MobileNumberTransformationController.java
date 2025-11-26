package cs.controller;

import cs.service.MobileNumberTransformationService;
import hxc.ecds.protocol.rest.MobileNumberFormatConfig;
import hxc.ecds.protocol.rest.MobileNumberFormatMapping;
import hxc.ecds.protocol.rest.MobileNumberTransformationProgress;
import hxc.ecds.protocol.rest.TransactionServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/mobile_number_transformation")
public class MobileNumberTransformationController {
	@Autowired
	private MobileNumberTransformationService service;

	@RequestMapping(value = "mapping", method = GET, produces = APPLICATION_JSON_VALUE)
	public MobileNumberFormatMapping getMapping() throws Exception {
		return service.getMapping();
	}
	
	@RequestMapping(value = "mapping", method = POST, produces = APPLICATION_JSON_VALUE)
	public MobileNumberFormatMapping updateMapping(@RequestBody MobileNumberFormatMapping mapping) throws Exception {
		return service.updateMapping(mapping);
	}

	@RequestMapping(value = "config", method = GET, produces = APPLICATION_JSON_VALUE)
	public MobileNumberFormatConfig getConfig() throws Exception {
		return service.getConfig();
	}
	
	@RequestMapping(value = "config", method = POST, produces = APPLICATION_JSON_VALUE)
	public MobileNumberFormatConfig updateConfig(@RequestBody MobileNumberFormatConfig config) throws Exception {
		return service.updateConfig(config);
	}

	@RequestMapping(value = "start", method = POST, produces = APPLICATION_JSON_VALUE)
	public TransactionServerResponse start() throws Exception {
		return service.start();
	}

	@RequestMapping(value = "stop", method = POST, produces = APPLICATION_JSON_VALUE)
	public TransactionServerResponse stop() throws Exception {
		return service.stop();
	}

	@RequestMapping(value = "progress", method = GET, produces = APPLICATION_JSON_VALUE)
	public MobileNumberTransformationProgressWrapper getProgress() throws Exception {
		/**
		 * THIS RETURNS A SERIALIZED LOCAL PRIVATE CLASS
		 * The PUBLIC getters for PRIVATE methods are used to return JSON in the REST response
		 * SEE BELOW
		 */
		return new MobileNumberTransformationProgressWrapper(service.getProgress());
	}

	@RequestMapping(value = "dual_phase/enable", method = POST, produces = APPLICATION_JSON_VALUE)
	public TransactionServerResponse enableDualPhase(@QueryParam("force") boolean force) throws Exception {
		return service.enableDualPhase(force);
	}

	@RequestMapping(value = "dual_phase/disable", method = POST, produces = APPLICATION_JSON_VALUE)
	public TransactionServerResponse disableDualPhase(@QueryParam("force") boolean force) throws Exception {
		return service.disableDualPhase(force);
	}
	
	private static class MobileNumberTransformationProgressWrapper {
		public MobileNumberTransformationProgressWrapper(MobileNumberTransformationProgress progress) {
			this.progress = progress;
			this.consoleMessage = progress.toString();
		}

		private MobileNumberTransformationProgress progress;
		private String consoleMessage;

		/**
		 * We do actually use these -- they are sent using the GETTERS to the REST response in JSON
		 */
		@SuppressWarnings("unused")
		public String getConsoleMessage() {
			return consoleMessage;
		}

		@SuppressWarnings("unused")
		public void setConsoleMessage(String consoleMessage) {
			this.consoleMessage = consoleMessage;
		}

		@SuppressWarnings("unused")
		public MobileNumberTransformationProgress getProgress() {
			return progress;
		}

		@SuppressWarnings("unused")
		public void setProgress(MobileNumberTransformationProgress progress) {
			this.progress = progress;
		}
	}
}
