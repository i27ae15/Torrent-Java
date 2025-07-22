package coder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class Encoder {

    /**
     * Main public method to encode any supported Java object into a Bencode string.
     * This acts as a dispatcher, calling the appropriate private helper.
     *
     * @param obj The object to encode (String, Long, Integer, List, or Map).
     * @return The Bencoded string.
     */
    public static String encode(Object obj) {
        if (obj instanceof String) {
            return encodeString((String) obj);
        } else if (obj instanceof Long) {
            return encodeLong((Long) obj);
        } else if (obj instanceof Integer) {
            // Automatically handle integers by converting them to longs
            return encodeLong(((Integer) obj).longValue());
        } else if (obj instanceof List) {
            return encodeList((List<?>) obj);
        } else if (obj instanceof Map) {
            // We assume the map has String keys, which is required by the spec
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) obj;
            return encodeDictionary(map);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + obj.getClass().getName());
        }
    }

    private static String encodeString(String str) {
        // Using getBytes is more accurate for length calculation than str.length()
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return bytes.length + ":" + str;
    }

    private static String encodeLong(long number) {
        return "i" + number + "e";
    }

    private static String encodeList(List<?> list) {
        StringBuilder encodedList = new StringBuilder();
        for (Object item : list) {
            // Delegate to the main encode method to avoid repeating logic
            encodedList.append(encode(item));
        }
        return "l" + encodedList.toString() + "e";
    }

    private static String encodeDictionary(Map<String, ?> dictionary) {
        // Bencode requires dictionary keys to be sorted alphabetically.
        List<String> sortedKeys = new ArrayList<>(dictionary.keySet());
        Collections.sort(sortedKeys);

        StringBuilder encodedDictionary = new StringBuilder();
        for (String key : sortedKeys) {
            Object value = dictionary.get(key);
            // Append the encoded key, then the encoded value
            encodedDictionary.append(encodeString(key));
            encodedDictionary.append(encode(value)); // Delegate for the value
        }

        return "d" + encodedDictionary.toString() + "e";
    }
}