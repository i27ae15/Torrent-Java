package utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlUtils {

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String urlEncodeBytes(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            int unsignedByte = b & 0xFF; // Convert to unsigned

            // Check if the byte needs encoding
            if (isUnreserved(unsignedByte)) {
                result.append((char) unsignedByte);
            } else {
                result.append('%');
                result.append(String.format("%02X", unsignedByte));
            }
        }

        return result.toString();
    }

    private static boolean isUnreserved(int b) {
        return (b >= 'A' && b <= 'Z') ||
               (b >= 'a' && b <= 'z') ||
               (b >= '0' && b <= '9') ||
               b == '-' || b == '_' || b == '.' || b == '~';
        }

}
