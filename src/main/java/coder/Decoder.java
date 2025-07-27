package coder;

import java.util.ArrayList;
import java.util.HashMap;

import utils.BencodingTypes.DecodedDictionary;
import utils.BencodingTypes.DecodedList;
import utils.BencodingTypes.DecodedValue;

public class Decoder {

    private static DecodedValue decodeString(String bencodedString) {
        return decodeString(bencodedString, 0);
    }

    private static DecodedValue decodeString(String bencodedString, int startIdx) {
        int firstColonIndex = 0;
        int totalLength = 0;

        for(int i = startIdx; i < bencodedString.length(); i++) {
            totalLength++;
            if(bencodedString.charAt(i) == ':') {
                firstColonIndex = i;
                break;
            }
        }

        int length = Integer.parseInt(bencodedString.substring(startIdx, firstColonIndex));
        String str = bencodedString.substring(firstColonIndex+1, firstColonIndex+length+1);
        totalLength += str.length();

        return new DecodedValue(str, totalLength);
    }

    private static DecodedValue decodeNumber(String bencodedString) {
        return decodeNumber(bencodedString, 0);
    }

    private static DecodedValue decodeNumber(String bencodedString, int startIdx) {
        String str = bencodedString.substring(startIdx + 1, bencodedString.indexOf("e", startIdx));
        return new DecodedValue(str, str.length() + 2);
    }

    private static DecodedList decodeList(String bencodedList, int startIdx) {
        ArrayList<Object> decodedList = new ArrayList<>();

        int decodedSize = 0;
        int length = 0;

        Object value;

        for (int i = startIdx; i < bencodedList.length(); i++) {
            char currentChar = bencodedList.charAt(i);

            if (Character.isDigit(currentChar)) {
                DecodedValue currentDecoded = decodeString(bencodedList, i);
                length = currentDecoded.length() - 1;
                value = currentDecoded.value();

            } else {

                switch (currentChar) {
                    case 'i':
                        DecodedValue currentDecoded = decodeNumber(bencodedList, i);
                        length = currentDecoded.length() - 1;
                        value = Long.parseLong(currentDecoded.value());
                        break;

                    case 'l':
                        DecodedList list = decodeList(bencodedList, i + 1);
                        length += list.length();
                        value = list.list();
                        break;

                    case 'd':
                        DecodedDictionary dictionary = decodeDictionary(bencodedList, i + 1);
                        length += dictionary.length();
                        value = dictionary.dictionary();
                        break;

                    case 'e':
                        return new DecodedList(decodedList, decodedSize);

                    default:
                        continue;
                }

            }

            i += length;
            decodedSize += i;
            decodedList.add(value);

        }

        return new DecodedList(decodedList, decodedSize);

    }

    private static DecodedDictionary decodeDictionary(String bencodedDictionary, int startIdx) {
        HashMap<String, Object> decodedDictionary = new HashMap<>();
        int decodedSize = 0;
        int length = 0;

        String key = null;
        Object value = null;

        for (int i = startIdx; i < bencodedDictionary.length(); i++) {
            char currentChar = bencodedDictionary.charAt(i);

            if (Character.isDigit(currentChar)) {
                DecodedValue currentDecoded = decodeString(bencodedDictionary, i);
                length = currentDecoded.length() - 1;

                if (key == null) {
                    key = currentDecoded.value();
                } else {
                    value = currentDecoded.value();
                }

            } else {

                switch (currentChar) {
                    case 'i':
                        DecodedValue currentDecoded = decodeNumber(bencodedDictionary, i);
                        length = currentDecoded.length() - 1;
                        value = Long.parseLong(currentDecoded.value());
                        break;

                    case 'l':
                        DecodedList list = decodeList(bencodedDictionary, i + 1);
                        length += list.length();
                        value = list.list();
                        break;

                    case 'd':
                        DecodedDictionary dictionary = decodeDictionary(bencodedDictionary, i + 1);
                        length += dictionary.length();
                        value = dictionary.dictionary();
                        break;

                    case 'e':
                        return new DecodedDictionary(decodedDictionary, decodedSize);

                    default:
                        continue;
                }

            }

            i += length;
            decodedSize += i;

            if (key != null && value != null) {

                // System.out.println("KEY: " + key);
                // System.out.println("VALUE: " + value);

                decodedDictionary.put(key, value);
                key = null;
                value = null;
            }
        }

        return new DecodedDictionary(decodedDictionary, decodedSize);

    }

    public static Object decodeBencode(String bencodedString) {

        char initialChar = bencodedString.charAt(0);
        char lastChar = bencodedString.charAt(bencodedString.length() - 1);

        if (Character.isDigit(initialChar)) {
            return decodeString(bencodedString).value();
        }

        else if (lastChar == 'e') {

            switch (initialChar) {
                case 'i':
                    return Long.parseLong(decodeNumber(bencodedString).value());

                case 'l':
                    return decodeList(bencodedString, 1).list();

                case 'd':
                    return decodeDictionary(bencodedString, 1).dictionary();

                default:
                    break;
            }
        }

        throw new RuntimeException("Unsupported bencode format");
    }

}
