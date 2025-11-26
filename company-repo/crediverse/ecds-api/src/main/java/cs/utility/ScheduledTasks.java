//package cs.utility;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
//@Component
//public class ScheduledTasks {
//    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
//    private static final double kiBsinMiB = 1024;
//
//    @Autowired
//    MetricsEndpoint metricsEndpoint;
//
//    @Scheduled(initialDelay = 10000, fixedRate = 10000)
//    public void logMetrics() {
//        Map<String, Object> metrics = metricsEndpoint.invoke();
//        StringBuilder sb = new StringBuilder("Metrics: ");
//
//        sb.append(String.format("systemload.average: %.2f, ", Double.parseDouble(String.valueOf(metrics.get("systemload.average")))));
//        sb.append("mem: ").append(kiBtoMiB(metrics.get("mem")));
//        sb.append("mem.free: ").append(kiBtoMiB(metrics.get("mem.free")));
//        sb.append("heap.used: ").append(kiBtoMiB(metrics.get("heap.used")));
//        sb.append("threads: ").append(metrics.get("threads")).append(", ");
//        sb.append("threads.peak: ").append(metrics.get("threads.peak")).append(", ");
//
//        logger.info(sb.toString());
//    }
//
//    private String kiBtoMiB(Object value) {
//        return String.format("%.2f MiB, ", Integer.parseInt(String.valueOf(value)) / kiBsinMiB);
//    }
//}
