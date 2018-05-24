package myftpserver;

import java.util.HashMap;

public class User {
    public static ThreadLocal<Boolean> getIsLogin() {
        return isLogin;
    }

    public static void setIsLogin(ThreadLocal<Boolean> isLogin) {
        User.isLogin = isLogin;
    }

    private String username;
    private String password;
    static ThreadLocal<Boolean> isLogin =  new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue(){
            return false;
        }
    };



    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String ,String> users = new HashMap<>();

    public void createUser(){
        users.put("chauyu","123456");
        users.put("admin","123456");
    }





}
