package example;

import org.junit.Test;

import java.io.IOException;

public class GossipTests {
    @Test
    public void gossipTest1() throws IOException, InterruptedException {
        GossipConnectionHandler a = new GossipConnectionHandler("Alice");
        GossipConnectionHandler b = new GossipConnectionHandler("Bob");
        GossipConnectionHandler c = new GossipConnectionHandler("Clara");

        int portNumber = 7777;

        // a-b
        TCPChannel a2b = new TCPChannel(portNumber, true, "a2b");
        TCPChannel b2a = new TCPChannel(portNumber, false, "b2a");

        // a-c
        portNumber++;
        TCPChannel a2c = new TCPChannel(portNumber, true, "a2c");
        TCPChannel c2a = new TCPChannel(portNumber, false, "c2a");

        // b-c
        portNumber++;
        TCPChannel b2c = new TCPChannel(portNumber, true, "b2c");
        TCPChannel c2b = new TCPChannel(portNumber, false, "c2b");

        // start
        a2b.start();
        b2a.start();

        a2c.start();
        c2a.start();

        b2c.start();
        c2b.start();

        // wait to connect
        a2b.waitForConnection();
        b2a.waitForConnection();

        a2c.waitForConnection();
        c2a.waitForConnection();

        b2c.waitForConnection();
        c2b.waitForConnection();

        // connect to application
        a.handleConnection(a2b.getInputStream(), a2b.getOutputStream());
        b.handleConnection(b2a.getInputStream(), b2a.getOutputStream());

        a.handleConnection(a2c.getInputStream(), a2c.getOutputStream());
        c.handleConnection(c2a.getInputStream(), c2a.getOutputStream());

        b.handleConnection(b2c.getInputStream(), b2c.getOutputStream());
        c.handleConnection(c2b.getInputStream(), c2b.getOutputStream());

        // finally - start gossip
        a.sendGossipMessage("Have your heard... wow.");

        Thread.sleep(100);
    }
}
