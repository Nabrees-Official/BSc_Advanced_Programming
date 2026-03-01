package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String sql = "SELECT username, role FROM users WHERE username = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");

                    HttpSession session = req.getSession(true);
                    session.setAttribute("username", username);
                    session.setAttribute("role", role);

                    resp.sendRedirect("menu"); // go to dashboard
                    return;
                }
            }

            resp.setContentType("text/html");
            resp.getWriter().println("""
                    <html><body style="font-family:Arial; padding:20px;">
                    <h2>❌ Invalid username or password</h2>
                    <p><a href="login.html">Try Again</a></p>
                    </body></html>
                    """);

        } catch (Exception e) {
            resp.setContentType("text/html");
            resp.getWriter().println("""
                    <html><body style="font-family:Arial; padding:20px;">
                    <h2>❌ Error: %s</h2>
                    <p><a href="login.html">Try Again</a></p>
                    </body></html>
                    """.formatted(e.getMessage()));
        }
    }
}