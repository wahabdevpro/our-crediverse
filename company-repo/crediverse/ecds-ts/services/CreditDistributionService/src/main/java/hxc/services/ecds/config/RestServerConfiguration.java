package hxc.services.ecds.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import featureBar.FeatureBarClient;

public class RestServerConfiguration
{
    private static RestServerConfiguration instance = null;
    private Properties properties = null;

    final static Logger logger = LoggerFactory.getLogger(RestServerConfiguration.class);

//    FeatureBarClient featureBarClient = null;

    private RestServerConfiguration() {
        try(InputStream input = RestServerConfiguration.class.getClassLoader().getResourceAsStream("application.properties")){
            properties = new Properties();
            if (input == null) {
                logger.error("Unable to find application.properties file");
            }
            properties.load(input);
        }catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static synchronized RestServerConfiguration getInstance(){
        if (instance == null)
            instance = new RestServerConfiguration();
        return instance;
    }

    public String getSpecificProperty(String propertyKey){
        String value = this.properties.getProperty(propertyKey);
        if (value == null || value.isEmpty() ) {
            logger.error("Unable to locate Property {}", propertyKey);
            return "";
        }
        return value;
    }

    /*
    private FeatureBarClient getFeatureBarClient() {
        if (this.featureBarClient == null) {
            String clientKeyFilename = System.getProperty("user.dir")+"/tls/client.key.pem";
            String clientCertificateFilename = System.getProperty("user.dir")+"/tls/client.crt";
            String certificateAuthorityCertificateFilename = System.getProperty("user.dir")+"/tls/concurrent_ca.crt";

            this.featureBarClient = new FeatureBarClient(
                "https://featurebar:2379",
                clientKeyFilename,
                clientCertificateFilename,
                certificateAuthorityCertificateFilename);
        }

        return this.featureBarClient;
    }
    */

    public Boolean isEnabledMobileMoney() {
        //FeatureBarClient fbc = this.getFeatureBarClient();
        //Boolean isEnabled = fbc.isFeatureAvailable("crediverse.mobileMoneyNotificationFeature");
        return true; //isEnabled;
    }

}

