package Parser.Exceptions;

import java.io.IOException;

public class DiscoveringPeerException extends RuntimeException {

    public DiscoveringPeerException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public DiscoveringPeerException(InterruptedException e) {

        super("InterruptedException while discovering Peers: " + e.getMessage());
    }

    public DiscoveringPeerException(IOException e) {

        super("IOException while discovering Peers: " + e.getMessage());
    }

}
