package Peers.Exceptions;

public class HandshakeException extends RuntimeException {

    public HandshakeException(String ipAndPort, Throwable cause) {
        super("Error while doing handshake with peer at address: " + ipAndPort + " | msg: " + cause.getMessage());
    }

}
