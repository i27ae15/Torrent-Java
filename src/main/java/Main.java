import com.google.gson.Gson;
// import com.dampcake.bencode.Bencode; - available if you need it!
import decoder.Decoder;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

      String command = args[0];

      switch (command) {
        case "decode":
          String bencodedValue = args[1];
          Object decoded;

          try {
            decoded = Decoder.decodeBencode(bencodedValue);
          } catch(RuntimeException e) {
            System.out.println(e.getMessage());
            return;
          }
          System.out.println(gson.toJson(decoded));

          break;

        default:

          System.out.println("Unknown command: " + command);
          break;

    }
  }
}
