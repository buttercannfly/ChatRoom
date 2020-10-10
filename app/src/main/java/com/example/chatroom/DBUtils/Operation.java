package com.example.chatroom.DBUtils;

public class Operation {
    public static boolean verify(String username, String password){
        if(username.equals("admin")||username.equals("user")){
            return true;
        }
        return false;
    }
}
