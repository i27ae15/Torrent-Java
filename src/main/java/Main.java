import com.google.gson.Gson;

import Parser.TorrentFileInfo;
import coder.Decoder;


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
          TorrentFileInfo.getInfo(fileName);
          break;

        default:

          System.out.println("Unknown command: " + command);
          return;
    }



  }
}
