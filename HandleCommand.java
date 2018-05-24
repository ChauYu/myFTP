package myftpserver;

import java.io.*;
import java.lang.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class HandleCommand {

    String[] input;
    PrintWriter writer;
    Socket socket;
    User user = new User();

    static ThreadLocal<Integer>  port;
    static ThreadLocal<String> ip;

    //String despath = System.getProperty("user.dir");
    String rootpath = "D:"+File.separator+"FTP";
    //static String nowpath = "D:"+File.separator+"FTP";

    static ThreadLocal<String> nowpath=  new ThreadLocal<String>(){
        @Override
        protected String initialValue(){
            return "D:"+File.separator+"FTP";
        }
    };


    public HandleCommand(String[] input, PrintWriter writer,Socket socket){
        this.input = input;
        this.writer = writer;
        this.socket = socket;
    }


    public void optsCommand(){
        System.out.println("get opts");
        writer.write("200 OPTS UTF8 command success -  UTF8 encoding now ON.\r\n");
        writer.flush();
    }

    public void userCommand(){

        String name = input[1];
        System.out.println("name "+name);
        try {

            if(user.users.containsKey(name)){
                writer.write("331 User name okay, need password.\r\n");
                writer.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String password = br.readLine();

                String[] temp = password.split(" ");
                String realpassword = temp[1];

                if(realpassword.equals(user.users.get(name)))
                {
                    user.isLogin.set(true);
                    writer.write("230 User logged in, proceed.\r\n");
                    writer.flush();
                }else {
                    writer.write("501 Syntax error in parameters or arguments.\r\n");
                    writer.flush();
                    writer.write("530 Not logged in\r\n");
                    writer.flush();
                    socket.close();
                }

            }else{
                user.isLogin.set(false);
                writer.write("501 Syntax error in parameters or arguments.\r\n");
                writer.flush();
                writer.write("530 Not logged in\r\n");
                writer.flush();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void portCommand(){
        writer.write("200 PORT command successful.\r\n");
        writer.flush();

        String string = input[1];
        final String[] nums = string.split(",");

        //ip=nums[0]+"."+nums[1]+"."+nums[2]+"."+nums[3];
        //ip.set(nums[0]+"."+nums[1]+"."+nums[2]+"."+nums[3]);

        ip =  new ThreadLocal<String>(){
            @Override
            protected String initialValue(){
                return nums[0]+"."+nums[1]+"."+nums[2]+"."+nums[3];
            }
        };

        System.out.println("port内 ip = "+ip.get());

        //port = Integer.parseInt(nums[4])*256+Integer.parseInt(nums[5]);
        //port.set(Integer.parseInt(nums[4])*256+Integer.parseInt(nums[5]));


        port =  new ThreadLocal<Integer>(){
            @Override
            protected Integer initialValue(){
                return Integer.parseInt(nums[4])*256+Integer.parseInt(nums[5]);
            }
        };

        System.out.println("port内 port = "+port.get());

    }


    public void lsCommand(){

        System.out.println("nowpath= "+nowpath.get());

        File file = new File(nowpath.get());
        File[] files = file.listFiles();

        try {

            System.out.println("ls内 ip = "+ip.get());
            System.out.println("ls内 port = "+port.get());
            //传输数据
            Socket newsocket = new Socket(ip.get(),port.get());

            writer.write("125 Data connection already open; transfer starting.\r\n");
            writer.flush();

            PrintWriter datawriter = new PrintWriter(newsocket.getOutputStream());

            for(int i=0;i<files.length;i++){
                    System.out.println("file name : "+files[i].getName());
                    datawriter.write(files[i].getName()+"\r\n");
                    datawriter.flush();
            }

            newsocket.close();


            writer.write("226 Closing data connection,transfer complete.\r\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void getCommand(){

        try {

            String getfile = makeString(input);
            System.out.println("getfile = "+getfile);

            String finalpath = nowpath.get()+File.separator+getfile;
            System.out.println("finalpath = "+finalpath);


            File file = new File(finalpath);
            if (file.exists()) {

                if (file.isFile()) {

                    //传输数据
                    Socket newsocket = new Socket(ip.get(),port.get());

                    writer.write("125 Data connection already open; transfer starting.\r\n");
                    writer.flush();

                    BufferedOutputStream bos = new BufferedOutputStream(newsocket.getOutputStream());
                    byte[] buf = new byte[1024];
                    InputStream is = new FileInputStream(file);

                    while (is.read(buf) != -1) {
                        bos.write(buf);
                    }
                    bos.flush();

                    newsocket.close();

                    writer.write("226 Closing data connection,transfer complete.\r\n");
                    writer.flush();
                } else if (file.isDirectory()) {
                    writer.write("550 Requested action not taken.\r\n");
                    writer.flush();
                }


            }else {

                writer.write("550-The system cannot find the file specified.\r\n");
                writer.flush();
                writer.write("550 End.\r\n");
                writer.flush();

            }



        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public void dirCommand(){

        File file = new File(nowpath.get());
        File[] files = file.listFiles();

        try {

            //传输数据
            Socket newsocket = new Socket(ip.get(),port.get());

            writer.write("125 Data connection already open; transfer starting.\r\n");
            writer.flush();

            PrintWriter datawriter = new PrintWriter(newsocket.getOutputStream());

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long lastModifiedTime;


            for(int i=0;i<files.length;i++){

                String name = files[i].getName();
                long length = files[i].length();

                lastModifiedTime = files[i].lastModified();
                calendar.setTimeInMillis(lastModifiedTime);

                String time = sdf.format(calendar.getTime());

                if(files[i].isDirectory()) datawriter.write(time+"\t"+"<DIR>"+"\t"+length+"\t"+name+"\r\n");
                else datawriter.write(time+"\t"+"\t"+length+"\t"+name+"\r\n");

                datawriter.flush();

            }

            newsocket.close();

            writer.write("226 Closing data connection,transfer complete.\r\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void pwdCommand(){
        System.out.println(nowpath.get());
        writer.write(nowpath.get()+"\r\n");
        writer.flush();

    }

    public void cdCommand(){

        String[] tmp = nowpath.get().split("\\\\");
        StringBuffer buf = new StringBuffer();

        String path = makeString(input);
        System.out.println("cd 后面 "+path);


        if(path.equals("\\")||path.equals("/")){
            nowpath.set("D:"+File.separator+"FTP");
            writer.write("250 CWD Command successful.\r\n");
            writer.flush();
        }else if(path.equals("..")){

            if(!nowpath.get().equals(rootpath)){
                for(int i=0;i<tmp.length-1;i++){
                    buf.append(tmp[i]);
                    if(i!=tmp.length-1-1) buf.append(File.separator);
                }

                nowpath.set(buf.toString());
                writer.write("250 CWD Command successful.\r\n");
                writer.flush();

            }

        }else {
            File file = new File(nowpath.get());
            File[] files = file.listFiles();
            boolean exit = false;
            for(int i=0;i<files.length;i++){
                if(files[i].isDirectory()){
                    if(files[i].getName().equals(path)) exit = true;
                }
            }

            if(exit){
                for(int i=0;i<tmp.length;i++){
                    buf.append(tmp[i]);
                    buf.append(File.separator);
                    if(i==tmp.length-1){
                        buf.append(path);
                    }
                }

                nowpath.set(buf.toString());
                writer.write("250 CWD Command successful.\r\n");
                writer.flush();

            }else{
                writer.write("550-The system cannot find the file specified.\r\n");
                writer.flush();
                writer.write("550 End.\r\n");
                writer.flush();
            }


        }


    }


    public void quitCommand(){
        writer.write("221 Goodbye\r\n");
        writer.flush();
        try {
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //获得名字有空格的文件名
    public String makeString(String[] input){

        StringBuffer strbuf = new StringBuffer();

        for(int i=1;i<input.length;i++){
            strbuf.append(input[i]);
            if(i!=input.length-1) strbuf.append(" ");
        }

        String command = strbuf.toString();

        return command;
    }


}
