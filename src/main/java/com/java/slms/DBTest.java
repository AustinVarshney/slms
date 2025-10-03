package com.java.slms;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBTest {
    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://127.0.0.1:3306/slms?useSSL=false";
            String user = "root";
            String pass = "Austin@123";
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected successfully!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}