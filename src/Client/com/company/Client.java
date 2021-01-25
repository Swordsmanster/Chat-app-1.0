package com.company;
import java.net.*;
import java.io.*;


public class Client {
    Socket socket;
    public DataInputStream in;
    public DataOutputStream out;
    String nickname;
    public Client(String svIP, String nick){
        try{
            nickname = nick;
            socket = new Socket(svIP, 7633);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            send(nickname + " has connected\n");
        }catch(SocketException e){
            disconnect();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg){
        try{
            if(this.out != null){
                out.writeUTF(msg);
                out.flush();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try{
            if(this.out != null){
                out.writeUTF(nickname + " has disconnected\n");
                out.flush();
            }
            while(in.available() > 0){

            }
            socket.close();
        }catch(SocketException e){
            try{
                socket.close();
            }catch(IOException z) {
                z.printStackTrace();
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
