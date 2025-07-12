package utils;

import java.util.ArrayList;
import java.util.HashMap;

public final class BencodingTypes {
    private BencodingTypes() {} // Prevent instantiation

    public record DecodedValue(String value, int length) {}

    public record DecodedList(ArrayList<Object> list, int length) {}

    public record DecodedDictionary(HashMap<String, Object> dictionary, int length) {}

}