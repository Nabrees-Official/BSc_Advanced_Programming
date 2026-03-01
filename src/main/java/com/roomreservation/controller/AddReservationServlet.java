package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddReservationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String guestName = req.getParameter("guest_name");
        String address = req.getParameter("address");
        String contactNo = req.getParameter("contact_no");
        String roomType = req.getParameter("room_type");
        String checkIn = req.getParameter("check_in");
        String checkOut = req.getParameter("check_out");

        String message;

        String sql = "INSERT INTO reservations (guest_name, address, contact_no, room_type, check_in, check_out) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, guestName);
            ps.setString(2, address);
            ps.setString(3, contactNo);
            ps.setString(4, roomType);
            ps.setString(5, checkIn);
            ps.setString(6, checkOut);

            ps.executeUpdate();
            message = "✅ Reservation Added Successfully!";

        } catch (Exception e) {
            message = "❌ Error: " + e.getMessage();
        }

        resp.setContentType("text/html");
        resp.getWriter().println("""
                <html><body style="font-family:Arial; padding:20px;">
                <h2>%s</h2>
                <p><a href="addReservation.html">Add Another</a></p>
                <p><a href="hello">Back Home</a></p>
                </body></html>
                """.formatted(message));
    }
}