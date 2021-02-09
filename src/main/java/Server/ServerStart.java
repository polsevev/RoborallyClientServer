package Server;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;


public class ServerStart {

    static boolean recievedAnswer = false;
    static Server server;
    static int udpPort = 7969, tcpPort = 7878;

    public static void main(String[] args) throws Exception{
        server = new Server();

        server.bind(tcpPort, udpPort);
        server.getKryo().register(Packet.class);
        server.getKryo().register(Vector2.class);
        server.getKryo().register(WinPacket.class);
        server.start();
        System.out.println("Server is up and running");

        server.addListener(new Listener(){

            public void connected(Connection c){
                System.out.println("Client: " + c.getID() + " Just connected");
                Packet packet = new Packet();
                System.out.println("Sending ID to new Client " + c.getID());
                packet.ID = c.getID();
                c.sendTCP(packet);

            }
            public void received (Connection c, Object p){
                if(p instanceof Packet){
                    Packet packet = (Packet) p;
                    System.out.println("Client with ID: " + packet.playerThatMovedID + " Moved");
                    server.sendToAllExceptTCP(c.getID(), packet);

                }else if(p instanceof WinPacket){
                    WinPacket winPacket = (WinPacket) p;

                    server.sendToAllTCP(winPacket);
                }
            }

            public void disconnected(Connection c){
                System.out.println("Client disconnected");
            }
        });

        while(!recievedAnswer){
            Thread.sleep(1000);
        }


    }

}
