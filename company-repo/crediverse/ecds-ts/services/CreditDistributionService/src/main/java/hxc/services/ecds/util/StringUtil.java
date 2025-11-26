package hxc.services.ecds.util;

public class StringUtil {
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
