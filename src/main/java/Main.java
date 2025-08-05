import java.lang.reflect.Array;
import java.util.ArrayList;

import com.google.gson.Gson;

import Parser.TorrentFile;
import Peers.Peer;
import Peers.PeerConnection;
import Peers.PeerConnection.PeerMsg;
import Peers.PeerManager;
import coder.Decoder;


public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

      String command = args[0];
      Object decoded;

      String fileName;

      TorrentFile torrentFile;
      PeerManager peerManager;
      ArrayList<Peer> peers;
      String ipAddress;

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

          peerManager = new PeerManager(torrentFile);
          peerManager.discoverPeers();
          peers = peerManager.getPeers();

          for (Peer peer : peers) {
            System.out.println("Peer: " + peer.ipAddress() + ":" + peer.port());
          }

          break;

        case "handshake":

          fileName = args[1];
          torrentFile = new TorrentFile(fileName);

          ipAddress = args[2];

          peerManager = new PeerManager(torrentFile);
          peerManager.performHandshake(ipAddress);

          System.out.println("Peer ID: " + peerManager.getCurrentConnection().getPeerConnectedToIdString());

          break;

        case "download_piece":

          fileName = args[3];

          torrentFile = new TorrentFile(fileName);

          peerManager = new PeerManager(torrentFile);
          peerManager.discoverPeers();

          // Connecting to a peer
          peers = peerManager.getPeers();
          Peer peer = peers.get(0);
          peerManager.performHandshake(peer.ipAddress(), peer.port());

          PeerConnection connection = peerManager.getCurrentConnection();

          System.out.println("Peer ID: " + connection.getPeerConnectedToIdString());

          PeerMsg msg = connection.readPeerMsg();

          System.out.println("type: " + String.valueOf(msg.type()));
          System.out.println("payloadSize: " + String.valueOf(msg.payloadSize()));

          break;

        default:

          System.out.println("Unknown command: " + command);
          return;
    }



  }
}
