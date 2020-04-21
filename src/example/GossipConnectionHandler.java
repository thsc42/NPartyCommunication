package example;

import streamHandler.ConnectionHandler;

import java.io.*;
import java.util.*;

public class GossipConnectionHandler implements ConnectionHandler {
    private final String name;
    private HashMap<Integer, GossipReader> activeReader = new HashMap<>();
    private HashMap<Integer, OutputStream> activeOutputStreams = new HashMap<>();
    private int nextReadNumber = 0;

    private List<String> usedIDs = new ArrayList<>();

    public GossipConnectionHandler(String name) {
        this.name = name;
    }

    public void sendGossipMessage(String message) {
        Collection<OutputStream> openConnections = this.activeOutputStreams.values();
        if(openConnections.size() > 0) {
            // create id - very very simple
            long time = System.currentTimeMillis();
            int random = new Random().nextInt();

            StringBuilder idBuilder = new StringBuilder();
            idBuilder.append(time);
            idBuilder.append(random);

            String id = idBuilder.toString();

            // remember
            this.usedIDs.add(id);

            // create pdu
            GossipPDU gossipPDU = new GossipPDU(id, message);

            // send to all others
            for(OutputStream os : openConnections) {
                try {
                    gossipPDU.writePDU(os);
                } catch (IOException e) {
                    // should remove output stream (TODO)
                }
            }
        }
    }

    @Override
    public void handleConnection(InputStream is, OutputStream os) throws IOException {
        int id = nextReadNumber++;
        this.activeReader.put(id, new GossipReader(id, is));
        this.activeOutputStreams.put(id, os);
    }

    private synchronized void handlePDU(GossipPDU gossipPDU, GossipReader gossipReader) {
        // got pdu
        // already read this pdu?

        if(this.usedIDs.contains(gossipPDU.id)) {
            System.out.println("Log: id already exists: " + gossipPDU.id);
        } else {
            // now message do gossip
            System.out.println(this.name + ": got message: " + gossipPDU);
            List<Integer> deadIDs = new ArrayList<>();
            for(Integer id : activeOutputStreams.keySet()) {
                if(gossipReader.id != id) {
                    // don't send back to sender but anybody else
                    try {
                        gossipPDU.writePDU(activeOutputStreams.get(id));
                    } catch (IOException e) {
                        // problems with this output stream - remember
                        deadIDs.add(id);
                    }
                }
            }

            // remove dead connections
            for(Integer id : deadIDs) {
                this.activeOutputStreams.remove(id);
                GossipReader reader = this.activeReader.remove(id);
                reader.kill();
            }
        }
    }

    private synchronized void inputStreamDead(int id) {
        // kill and remove output stream as well
        OutputStream os = this.activeOutputStreams.remove(id);

        try {
            os.close();
        } catch (IOException e) {
            // did my best
        }
    }

    private class GossipReader extends Thread {
        private final InputStream is;
        private final int id;

        GossipReader(int id, InputStream is) {
            this.id = id;
            this.is = is;
            this.start(); // start yourself
        }

        public void run() {
            boolean again = true;
            while (again) {
                try {
                    // read pdu
                    GossipPDU gossipPDU = new GossipPDU(is);
                    // handle pdu
                    GossipConnectionHandler.this.handlePDU(gossipPDU, this);
                } catch (IOException e) {
                    // cannot recover from that.
                    GossipConnectionHandler.this.inputStreamDead(id);
                    again = false; // end thread
                }
            }
        }

        public void kill() {
            try {
                this.is.close();
            } catch (IOException e) {
                // cannot do anything here - did my best
            }
        }
    }

    private class GossipPDU {
        private String id;
        private String message;

        GossipPDU(String id, String message) {
            this.id = id;
            this.message = message;
        }

        GossipPDU(InputStream is) throws IOException {
            DataInputStream dis = new DataInputStream(is);
            this.id = dis.readUTF();
            this.message = dis.readUTF();
        }

        void writePDU(OutputStream os) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(id);
            dos.writeUTF(message);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("id: ");
            sb.append(this.id);
            sb.append(" | message: ");
            sb.append(this.message);
            return sb.toString();
        }
    }
}
