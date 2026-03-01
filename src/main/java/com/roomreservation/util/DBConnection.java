package com.roomreservation.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/room_reservation_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "12345";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            // load driver (important for Tomcat)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found!", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}