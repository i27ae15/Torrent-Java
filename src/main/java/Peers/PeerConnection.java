package Peers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HexFormat;

import Peers.Exceptions.HandshakeException;

public class PeerConnection {

    public record PeerMsg(int type, int payloadSize) {
    }

    private Socket socket;
    private byte[] buffer;
    private String peerId;
    private byte[] peerConnectedToId;
    private byte[] peerConnectedToHash;

    private String ip;
    private int port;

    private int bufferSize = 1024;


    public PeerConnection(String ip, int port, String peerId, byte[] torrentFileInfoHash) {
        this(ip, port, peerId, torrentFileInfoHash, true);
    }

    public PeerConnection(
        String ip,
        int port,
        String peerId,
        byte[] torrentFileInfoHash,
        boolean createConnection
    ) {

        this.resizeBuffer(this.bufferSize);

        this.ip = ip;
        this.port = port;
        this.peerId = peerId;

        if (createConnection) performHandshake(torrentFileInfoHash);
    }

    // Getters

    public String getPeerConnectedToHashString() {
        return HexFormat.of().formatHex(this.peerConnectedToHash);
    }

    public String getPeerConnectedToIdString() {
        return HexFormat.of().formatHex(this.peerConnectedToId);
    }

    public byte[] readFromBuffer(int from, int to) {
        return Arrays.copyOfRange(this.buffer, from, to);
    }

    public void resizeBuffer(int bufferSize) {

        if (bufferSize < 0) {
            throw new IllegalArgumentException("Buffer size cannot be less than 0");
        }

        this.bufferSize = bufferSize;
        buffer = new byte[this.bufferSize];
    }

    public void closeConnection() {

        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Socket failed while closing, maybe the socket was never connected?");
        }
    }

    public void performHandshake(byte[] torrentFileInfoHash) {
        // BitTorrent handshake format:
        // 1 byte: protocol length (19)
        // 19 bytes: "BitTorrent protocol"
        // 8 bytes: reserved (all zeros)
        // 20 bytes: info hash
        // 20 bytes: peer id

        String ipAndPort = this.ip + ":" + String.valueOf(this.port);
        System.err.println("connecting to peer at address: " + ipAndPort);

        try {
            this.socket = new Socket(ip, port);
            OutputStream output = socket.getOutputStream();

            output.write(19); // Protocol length
            output.write("BitTorrent protocol".getBytes());
            output.write(new byte[8]); // Reserved bytes (zeros)
            output.write(torrentFileInfoHash); // Your torrent's info hash
            output.write(peerId.getBytes()); // Your peer ID

            this.readFromConnection();

            // Extract peer's info hash (20 bytes)
            this.peerConnectedToHash = this.readFromBuffer(28, 48);
            this.peerConnectedToId = this.readFromBuffer(48, 68);

        } catch (IOException e) {

            throw new HandshakeException(ipAndPort, e);

        }
    }

    public void readFromConnection() {

        try {
            InputStream input = this.socket.getInputStream();
            input.read(this.buffer);
        } catch (IOException e) {
            System.err.println("Something went wrong filling up the buffer");
        }

    }

    public PeerMsg readPeerMsg() {
        readFromConnection();

        int payloadSize = (buffer[0] << 24) | (buffer[1] << 16) | (buffer[2] << 8) | buffer[3];
        int type;

        if (payloadSize == 0) {
            System.err.println("There is nothing to read");
            type = -1;
        } else {
            type = buffer[4];

        }

        return new PeerMsg(type, payloadSize);

    }

}
