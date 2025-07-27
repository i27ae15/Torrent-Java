package Peers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;
import java.net.URI;

import Parser.TorrentFile;
import Parser.Exceptions.DiscoveringPeerException;
import coder.Decoder;
import utils.UrlUtils;

public class Peer {

    TorrentFile torrentFile;

    public Peer (TorrentFile torrentFile) {
        this.torrentFile = torrentFile;
    }

    private String generatePeerId() {
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

        return peerId.toString();
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
            System.out.println("Peer: " + ipAddress + ":" + port);
        }
    }

    public void discoverPeers() {

        String baseUrl = (String) torrentFile.getFileInfo().get("announce");

        String params = "?info_hash=" + UrlUtils.urlEncodeBytes(torrentFile.getInfoHashBytes()) +
            "&peer_id=" + UrlUtils.urlEncode(generatePeerId()) +
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

}
