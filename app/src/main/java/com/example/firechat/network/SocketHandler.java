package com.example.firechat.network;

import io.socket.client.IO;
import io.socket.client.Socket;
import kotlin.jvm.Synchronized;

public class SocketHandler {
    static Socket mSocket;

    @Synchronized
    public static void setSocket(){
        try{
//            mSocket = IO.socket("http://65.2.3.45:3000");
            mSocket = IO.socket("http://10.0.2.2:3000");
        }catch (Exception e){

        }
    }

    @Synchronized
    static public Socket getSocket(){return mSocket;}
    @Synchronized
    static public void establishConnection(){mSocket.connect();}
    @Synchronized
    static public void closeConnection(){mSocket.disconnect();}

}
