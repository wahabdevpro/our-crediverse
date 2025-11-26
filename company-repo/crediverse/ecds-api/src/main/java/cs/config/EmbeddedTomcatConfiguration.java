package cs.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedTomcatConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
	
	@Value("${server.tomcat.basedir:/tmp}")
	private String tomcatBase;
	
	@Override
	public void customize(TomcatServletWebServerFactory container) {
		if (tomcatBase != null && !tomcatBase.equals("/tmp")) {
			File docBase = new File(tomcatBase+"-docbase");
			if (!docBase.exists() || !docBase.canWrite()) {
				docBase.mkdirs();
			}
			container.setDocumentRoot(docBase);
		}
	}
}
