package com.company;

import java.util.*;
import java.net.*;
import java.io.*;

public class Main {

    static ServerSocket svSocket;
    static Vector<Server> vec = new Vector<>();
    static Service service;

    public static void main(String[] args) {
        try {
            svSocket = new ServerSocket(7633);
        } catch (IOException e) {
            e.printStackTrace();
        }

        service = new Service();
        service.start();

        while (true) {

            try {
                Socket socket = svSocket.accept();
                if (socket != null) {
                    Server sv = new Server(socket);
                    vec.add(sv);
                    sv.start();
                    Iterator i = vec.iterator();
                    while (i.hasNext()) {
                        if (((Server) i.next()).isInterrupted()) {
                            ((Server) i.next()).out.flush();
                            ((Server) i.next()).client.close();
                            vec.remove(i.next());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    public static class Server extends Thread {
        Socket client;
        DataInputStream in;
        DataOutputStream out;

        public Server(Socket soc) {
            client = soc;
            try {
                in = new DataInputStream(soc.getInputStream());
                out = new DataOutputStream(soc.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                if (!client.isConnected()) {
                    this.interrupt();
                } else {
                    try {

                        String input = in.readUTF();
                        System.out.print(input);
                        service.write(input);
                        send(input);

                        if (!client.isConnected()) {
                            this.interrupt();
                            break;
                        }
                    } catch (EOFException e) {
                        this.interrupt();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return;
        }

        void send(String input) {
            for (int i = 0; i < vec.size(); i++) {
                try {
                    if (vec.get(i) != this) {
                        try {
                            vec.get(i).out.writeUTF(input);
                            vec.get(i).out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }


    static class Service extends Thread {
        Scanner sc = new Scanner(System.in);
        static FileWriter fw;
        BufferedWriter bw;

        public Service() {
            try {
                fw = new FileWriter("log.txt", true);
                bw = new BufferedWriter(fw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                String command = sc.nextLine();
                if (command.equals("exit")) {
                    try {
                        bw.append("\n /////////////////////////// \n\n");
                        bw.close();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < vec.size(); i++) {
                        try {
                            vec.get(i).out.flush();
                            while (vec.get(i).in.available() > 0) ;
                            vec.get(i).client.close();
                            vec.get(i).interrupt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.exit(0);
                }
            }

        }

        public void write(String message) {
            try {
                bw.append(message);
                bw.flush();
            } catch (IOException e) {

            }
        }
    }
}
