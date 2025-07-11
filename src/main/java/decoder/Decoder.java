package decoder;

public class Decoder {

    public static Object decodeBencode(String bencodedString) {

        // Decoding Strings
        if (Character.isDigit(bencodedString.charAt(0))) {
            int firstColonIndex = 0;

            for(int i = 0; i < bencodedString.length(); i++) {
                if(bencodedString.charAt(i) == ':') {
                    firstColonIndex = i;
                    break;
                }
            }

            int length = Integer.parseInt(bencodedString.substring(0, firstColonIndex));
            return bencodedString.substring(firstColonIndex+1, firstColonIndex+1+length);
        }

        // Decoding Integers
        if (bencodedString.charAt(0) == 'i' && bencodedString.charAt(bencodedString.length() - 1) == 'e') {
            String numberPart = bencodedString.substring(1, bencodedString.length() - 1);
            return Long.parseLong(numberPart);
        }

        throw new RuntimeException("Unsupported bencode format");

    }

}
