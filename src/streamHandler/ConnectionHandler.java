package streamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * When working with TCP, Bluetooth (namely RFComm) , or other stream based protocols -
 * it comes always down to working with streams. A specific library in combination with a specific
 * hardware (e.g. Bluetooth adapter, Ethernet-Card, ..) establishes a connection and return an
 * input and an output stream.
 *
 * In that very moment, application specific algorithms start. That's the interface between
 * stream based protocols and applications. This situation can be described by a very simple interfaces.
 * This one.
 */
public interface ConnectionHandler {
    void handleConnection(InputStream is, OutputStream os) throws IOException;
}
