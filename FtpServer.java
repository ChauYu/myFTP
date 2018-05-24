package myftpserver;

import java.io.*;
import java.net.*;
import java.lang.*;


public class FtpServer {

    /*
     * 要体现tcp的三次握手！！！！！！！！！！！！！！！！！
     * 一次只能多少个人访问
     */

    public void go(){

        try {

            ServerSocket serversocket = new ServerSocket(21);

            while (true) {

                Socket socket = serversocket.accept();

                System.out.println("connect");

                Handle handler = new Handle(socket);
                Thread t = new Thread(handler);
                t.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        FtpServer ftpserver = new FtpServer();
        ftpserver.go();

    }
}





