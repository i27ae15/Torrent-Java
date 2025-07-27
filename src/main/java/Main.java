import java.util.HexFormat;

import com.google.gson.Gson;

import Parser.TorrentFile;
import Peers.Peer;
import coder.Decoder;


public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

      String command = args[0];
      Object decoded;

      String fileName;

      TorrentFile torrentFile;
      Peer peer;

      switch (command) {
        case "decode":
          String bencodedValue = args[1];
          decoded = Decoder.decodeBencode(bencodedValue);

          System.out.println(gson.toJson(decoded));
          break;

        case "info":
          fileName = args[1];
          torrentFile = new TorrentFile(fileName);
          torrentFile.printFileInfo();
          break;

        case "peers":

          fileName = args[1];
          torrentFile = new TorrentFile(fileName);

          peer = new Peer(torrentFile);
          peer.discoverPeers();

          break;

        case "handshake":

          fileName = args[1];
          torrentFile = new TorrentFile(fileName);

          String ip = args[2];

          peer = new Peer(torrentFile);
          peer.performHandshake(ip);

          System.out.println("Peer ID: " + peer.getPeerConnectedToIdString());

          break;

        default:

          System.out.println("Unknown command: " + command);
          return;
    }



  }
}
