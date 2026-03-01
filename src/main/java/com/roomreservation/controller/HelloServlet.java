package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String dbStatus;

        try (Connection con = DBConnection.getConnection()) {
            dbStatus = "✅ MySQL Connected Successfully!";
        } catch (Exception e) {
            dbStatus = "❌ MySQL Connection Failed: " + e.getMessage();
        }

        resp.getWriter().println("""
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8"/>
                  <title>Room Reservation System</title>
                </head>
                <body style="font-family:Arial; padding:20px;">
                  <h2>Hello! Servlet is working ✅</h2>
                  <p><b>Database Status:</b> %s</p>
                </body>
                </html>
                """.formatted(dbStatus));
    }
}