package decoder;

import java.util.ArrayList;

import utils.ListIntPair;
import utils.StringIntPair;

public class Decoder {

    private static StringIntPair decodeString(String bencodedString) {
        return decodeString(bencodedString, 0);
    }

    private static StringIntPair decodeString(String bencodedString, int startIdx) {
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
        String str = bencodedString.substring(firstColonIndex+1, firstColonIndex+1+length);
        totalLength += str.length();

        return new StringIntPair(str, totalLength);
    }

    private static StringIntPair decodeNumber(String bencodedString) {
        return decodeNumber(bencodedString, 0);
    }

    private static StringIntPair decodeNumber(String bencodedString, int startIdx) {
        int index = bencodedString.indexOf("e", startIdx);

        System.err.println("STR: " + bencodedString + " | INDEX: " + index + " | START_IDX: " + startIdx);

        String str = bencodedString.substring(startIdx + 1, bencodedString.indexOf("e", startIdx));
        return new StringIntPair(str, str.length() + 2);
    }

    private static ListIntPair decodeList(String bencodedList, int startIdx) {
        ArrayList<Object> decodedList = new ArrayList<>();
        int decodedSize = 0;

        for (int i = startIdx; i < bencodedList.length(); i++) {
            StringIntPair currentDecoded = new StringIntPair("", 0);
            char currentChar = bencodedList.charAt(i);

            if (Character.isDigit(currentChar)) {
                currentDecoded = decodeString(bencodedList, i);
                decodedList.add(currentDecoded.first());

            } else if (currentChar == 'i') {
                currentDecoded = decodeNumber(bencodedList, i);
                decodedList.add(Long.parseLong(currentDecoded.first()));

            } else if (currentChar == 'l') {
                ListIntPair list = decodeList(bencodedList, i + 1);
                decodedList.add(list.first());
                i += list.second();
                continue;


            } else if (currentChar == 'e') {
                return new ListIntPair(decodedList, decodedSize);

            } else {
                continue;
            }

            i += currentDecoded.second() - 1;
            decodedSize += i;

        }

        return new ListIntPair(decodedList, decodedSize);

    }

    public static Object decodeBencode(String bencodedString) {

        if (Character.isDigit(bencodedString.charAt(0))) {
            return decodeString(bencodedString).first();
        }

        else if (bencodedString.charAt(0) == 'i' && bencodedString.charAt(bencodedString.length() - 1) == 'e') {
            return Long.parseLong(decodeNumber(bencodedString).first());
        }

        else if (bencodedString.charAt(0) == 'l' && bencodedString.charAt(bencodedString.length() - 1) == 'e') {
            // StartIdx at 1 to avoid L again
            return decodeList(bencodedString, 1).first();
        }

        throw new RuntimeException("Unsupported bencode format");

    }

}
