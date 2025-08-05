package Peers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;
import java.net.URI;

import Parser.TorrentFile;
import Parser.Exceptions.DiscoveringPeerException;
import Peers.PeerConnection.PeerMsg;
import coder.Decoder;
import utils.UrlUtils;

public class PeerManager {

    TorrentFile torrentFile;
    String peerId;

    private final ArrayList<Peer> peers = new ArrayList<>();
    private final ArrayList<PeerConnection> connections = new ArrayList<>();

    private PeerConnection currentConnection;

    // Constructor

    public PeerManager (TorrentFile torrentFile) {
        this.torrentFile = torrentFile;
        this.generatePeerId();
    }

    // Getters

    public ArrayList<Peer> getPeers() { return this.peers; }

    public PeerConnection getCurrentConnection() { return this.currentConnection; }

    // Other shit

    private void generatePeerId() {
        // BitTorrent peer IDs are 20 bytes
        // Common format: "-XX1234-" + 12 random characters
        // Where XX is client identifier (e.g., "UT" for µTorrent)

        StringBuilder peerId = new StringBuilder("-JV0001-"); // JV = Java, 0001 = version
        Random random = new Random();

        // Add 12 random alphanumeric characters
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 12; i++) {
            peerId.append(chars.charAt(random.nextInt(chars.length())));
        }

        this.peerId = peerId.toString();

    }

    private void processPeersResponse(String bencodedBody) {

        @SuppressWarnings("unchecked")
        HashMap<String, Object> body = (HashMap<String, Object>) Decoder.decodeBencode(bencodedBody);

        String peersString = (String) body.get("peers");
        byte[] peers = peersString.getBytes(StandardCharsets.ISO_8859_1);

        for (int i = 0; i < peers.length; i += 6) {
            int ip1 = peers[i] & 0xFF;
            int ip2 = peers[i + 1] & 0xFF;
            int ip3 = peers[i + 2] & 0xFF;
            int ip4 = peers[i + 3] & 0xFF;

            int port = ((peers[i + 4] & 0xFF) << 8) | (peers[i + 5] & 0xFF);
            String ipAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;

            this.peers.add(new Peer(port, ipAddress));

        }
    }

    public void discoverPeers() {

        String baseUrl = (String) torrentFile.getFileInfo().get("announce");

        String params = "?info_hash=" + UrlUtils.urlEncodeBytes(torrentFile.getInfoHashBytes()) +
            "&peer_id=" + UrlUtils.urlEncode(this.peerId) +
            "&port=6881" +
            "&uploaded=0" +
            "&downloaded=0" +
            "&left=" + torrentFile.getFileInsideInfo().get("length") +
            "&compact=1";

        String fullUrl = baseUrl + params;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(fullUrl))
        .GET()
        .build();

        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e ) {
            throw new DiscoveringPeerException(e);
        }

        // Convert to string using ISO-8859-1 (preserves all bytes)
        String bencodedBody = new String(response.body(), StandardCharsets.ISO_8859_1);

        // Rest stays the same
        processPeersResponse(bencodedBody);

    }

    public void performHandshake(String ipAndPort) {
        String ip = ipAndPort.split(":")[0];
        int port = Integer.parseInt(ipAndPort.split(":")[1]);

        performHandshake(ip, port);
    }

    public void performHandshake(String ip, int port) {
        // BitTorrent handshake format:
        // 1 byte: protocol length (19)
        // 19 bytes: "BitTorrent protocol"
        // 8 bytes: reserved (all zeros)
        // 20 bytes: info hash
        // 20 bytes: peer id

        this.currentConnection = new PeerConnection(ip, port, this.peerId, this.torrentFile.getInfoHashBytes());
        this.connections.add(this.currentConnection);

    }

}
