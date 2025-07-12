import java.util.HashMap;

import com.google.gson.Gson;

import decoder.Decoder;


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
          String fileContent = Parser.TorrentFile.getFileInfo(fileName);

          @SuppressWarnings("unchecked")
          HashMap<String, Object> fileInfo = (HashMap<String, Object>) Decoder.decodeBencode(fileContent);

          @SuppressWarnings("unchecked")
          HashMap<String, Object> info = (HashMap<String, Object>) fileInfo.get("info");

          System.out.println("Tracker URL: " + fileInfo.get("announce"));
          System.out.println("Length: " + info.get("length"));
          break;

        default:

          System.out.println("Unknown command: " + command);
          return;
    }



  }
}
