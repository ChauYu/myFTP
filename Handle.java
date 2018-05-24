package myftpserver;

import java.io.*;
import java.net.Socket;
import java.lang.*;

public class Handle implements Runnable{

    Socket socket;

    PrintWriter writer;
    BufferedReader br;

    public Handle(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run() {


        try {

            writer = new PrintWriter(socket.getOutputStream());
            InputStreamReader isr =  new InputStreamReader(socket.getInputStream());
            br = new BufferedReader(isr);

            int i=0;

            while (true) {

                if(i==0) {
                    writer.write("220 Service ready for new user\r\n");
                    System.out.println("第 " + i++ + " 次到这里");
                    writer.flush();
                }else {
                    System.out.println("第 " + i++ + " 次到另一边");
                    String command = br.readLine();
                    System.out.println("command from " + socket.getInetAddress() + " : " + command);

                    if (!command.equals(null)) {

                        String[] input = command.split(" ");
                        //System.out.println(input[0]);
                        judgeCommand(input);

                    } else {

                        //输入了quit命令
                        writer.write("221 Goodbye\r\n");
                        writer.flush();
                        br.close();
                        socket.close();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.close();
            try {
                br.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void judgeCommand(String[] input){


        String realcommand =  input[0];
        System.out.println("realcommand "+realcommand);
        HandleCommand handlecommand =new HandleCommand(input,writer,socket);
        handlecommand.user.createUser();

//        if(!realcommand.equals("USER")&&!realcommand.equals("QUIT")&&!realcommand.equals("OPTS")){
//            if(!handlecommand.user.getIsLogin().get()){
//                writer.write("530 Please login with USER and PASS.\r\n");
//                writer.flush();
//                return;
//            }else{
//
//            }
//        }

        if(!realcommand.equals("USER")&&
                !realcommand.equals("QUIT")&&
                !realcommand.equals("OPTS")&&
                !handlecommand.user.getIsLogin().get()){

            writer.write("530 Please login with USER and PASS.\r\n");
            writer.flush();
            return;

        }else {
            switch (realcommand){
                case "USER"://用户登陆,与PASS结合了
                    handlecommand.userCommand();
                    break;

                case "OPTS":
                    handlecommand.optsCommand();
                    break;

                case "PORT"://dir,get,ls
                    handlecommand.portCommand();
                    break;

                case "NLST"://ls,PORT以后
                    handlecommand.lsCommand();
                    break;

                case "RETR"://get,PORT以后
                    handlecommand.getCommand();
                    break;

                case "XPWD"://pwd,显示当前工作目录
                    handlecommand.pwdCommand();
                    break;

                case "CWD"://cd       cd \ 退回到服务器根目录 cd ..退回到上级目录
                    handlecommand.cdCommand();
                    break;

                case "LIST"://dir,PORT以后
                    handlecommand.dirCommand();
                    break;

                case "QUIT"://用户没有登录的时候quit
                    handlecommand.quitCommand();
                    break;

                default:
                    //仿佛不需要的样子 自动判断了
                    break;

            }
        }



    }

}