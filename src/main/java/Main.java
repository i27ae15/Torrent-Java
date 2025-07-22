import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;

import com.google.gson.Gson;

import coder.Decoder;
import coder.Encoder;


public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

      String command = args[0];
      Object decoded;

      switch (command) {
        case "decode":
          String bencodedValue = args[1];
          decoded = Decoder.decodeBencode(bencodedValue);

          System.out.println(gson.toJson(decoded));
          break;

        case "info":
          String fileName = args[1];

          // Read the file as raw bytes to avoid any character encoding issues.
          byte[] fileBytes = Files.readAllBytes(Paths.get(fileName));

          // For parsing, it's often easier to work with a string that preserves byte values.
          String fileContent = new String(fileBytes, StandardCharsets.ISO_8859_1);

          // --- You still need to decode to get metadata for printing ---
          @SuppressWarnings("unchecked")
          HashMap<String, Object> fileInfo = (HashMap<String, Object>) Decoder.decodeBencode(fileContent);

          @SuppressWarnings("unchecked")
          HashMap<String, Object> info = (HashMap<String, Object>) fileInfo.get("info");

          System.out.println("Tracker URL: " + fileInfo.get("announce"));
          System.out.println("Length: " + info.get("length"));
          // --- End of metadata printing ---


          // --- HASHING THE ORIGINAL DATA ---
          // 1. Find the starting position of the raw bencoded 'info' dictionary.
          // It comes right after its key, "4:info".
          String infoKey = "4:info";
          int infoStartIndex = fileContent.indexOf(infoKey) + infoKey.length();

          // 2. Extract the raw bencoded dictionary.
          // This is a bit of a shortcut. A robust Bencode parser would give you the
          // exact start and end index. This approach assumes the 'info' dictionary
          // is the last item in the main dictionary.
          String bencodedInfoString = fileContent.substring(infoStartIndex, fileContent.length() - 1);

          // 3. Get the raw bytes of THAT substring.
          byte[] bencodedInfoBytes = bencodedInfoString.getBytes(StandardCharsets.ISO_8859_1);

          // 4. Calculate the hash of the original bytes.
          MessageDigest digest = MessageDigest.getInstance("SHA-1");
          byte[] hashBytes = digest.digest(bencodedInfoBytes);

          // 5. Convert the byte array to a hexadecimal string (your existing code for this is correct)
          StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
          for (byte b : hashBytes) {
              String hex = Integer.toHexString(0xff & b);
              if (hex.length() == 1) {
                  hexString.append('0');
              }
              hexString.append(hex);
          }
          String infoHash = hexString.toString();

          System.out.println("Info Hash: " + infoHash);
          break;

        default:

          System.out.println("Unknown command: " + command);
          return;
    }



  }
}
