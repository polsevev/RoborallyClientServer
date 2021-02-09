package inf112.skeleton.app;

import Server.Packet;
import Server.WinPacket;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class NetworkClient {
    public static boolean recievedId = false;
    public int id;
    static Client client;
    static int udpPort = 7969, tcpPort = 7878;
    static String ip = "localhost";

    public NetworkClient(TiledTest tiledTest) throws Exception{
        client = new Client();
        client.getKryo().register(Packet.class);
        client.getKryo().register(Vector2.class);
        client.getKryo().register(WinPacket.class);
        client.start();
        System.out.println("Client is up and running");
        client.connect(5000, ip, tcpPort, udpPort);
        client.addListener(new Listener(){
            public void received (Connection c, Object p){
                if (p instanceof Packet){
                    Packet packet = (Packet) p;
                    if(!recievedId){
                        System.out.println("I am now the id of : " + packet.ID);
                        id = packet.ID;
                        recievedId = true;
                        tiledTest.playerSet(id);
                    }else{
                        System.out.println("Recieved this from server: " + packet.playerThatMovedID + " is in x = " + packet.v.x + " y = " + packet.v.y);
                        tiledTest.moveEnemies(packet.playerThatMovedID, packet.v);
                    }


                }else if(p instanceof WinPacket){
                    WinPacket winPacket = (WinPacket) p;
                    tiledTest.quit(winPacket.ID);
                }
            }
        });
    }

    public void sendCords(Vector2 v){
        Packet p = new Packet();
        p.playerThatMovedID = this.id;
        p.v = v;
        client.sendTCP(p);
    }
    public int getID(){
        return this.id;
    }

    public void sendWin(int id){
        WinPacket winPacket = new WinPacket();
        winPacket.ID = id;
        client.sendTCP(winPacket);
    }

}
