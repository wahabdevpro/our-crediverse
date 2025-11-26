package hxc.services.ecds.util;



public class ResponseUtils {
    private ResponseUtils() {} // prevent instantiation

    // Core append logic, ensures semicolon and null-safety
    public static String appendInfo(String current, String newInfo) {
        if (newInfo == null || newInfo.isEmpty()) return current;

        if (current == null || current.trim().isEmpty()) {
            return newInfo;
        } else {
            if ("garnishSOS=true".equalsIgnoreCase(newInfo) 
                    && current.toLowerCase().contains("garnishsos=true")) {
                return current;
            }

            if (!current.endsWith(";")) current += ";";
            return current + newInfo;
        }
    }

    // Combine multiple pieces of info into a single string
    public static String combineAdditionalInformation(String current, String... infos) {
        for (String info : infos) {
            current = appendInfo(current, info);
        }
        return current;
    }
    public static String getGarnishFlag(String requestOrigin) {
        if (requestOrigin == null) {
            return null;
        }
        return "coalesceairtime".equalsIgnoreCase(requestOrigin.trim())
                ? "garnishSOS=true"
                : null;
    }
}
