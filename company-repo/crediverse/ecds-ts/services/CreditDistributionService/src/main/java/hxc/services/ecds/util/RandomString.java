package hxc.services.ecds.util;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class RandomString {

    /**
     * Generate a random string.
     */
    public String nextString() {
        buf[0] = upper.charAt(random.nextInt(upper.length()));
        buf[1] = lower.charAt(random.nextInt(lower.length()));
        buf[2] = digits.charAt(random.nextInt(digits.length()));
        buf[3] = symbols.charAt(random.nextInt(symbols.length()));
        for (int idx = 4; idx < buf.length; ++idx)
                buf[idx] = indexTable[random.nextInt(indexTable.length)];
        for (int i = buf.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = buf[index];
            buf[index] = buf[i];
            buf[i] = temp;
        }    
        return new String(buf);
    }

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";
    
    public static final String symbols = "~!@#$%^&*";

    public static final String alphanum_and_symbols = upper + lower + digits + symbols;

    private final Random random;

    private final char[] indexTable;

    private final char[] buf;

    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.indexTable = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public RandomString(int length, Random random) {
        this(length, random, alphanum_and_symbols);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public RandomString() {
        this(12);
    }

}
