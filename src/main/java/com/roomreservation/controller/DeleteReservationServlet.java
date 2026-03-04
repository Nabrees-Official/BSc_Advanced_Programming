package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DeleteReservationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendRedirect("listReservations");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect("listReservations");
            return;
        }

        String sql = "DELETE FROM reservations WHERE reservation_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            // back to list
            resp.sendRedirect("listReservations");

        } catch (Exception e) {
            resp.getWriter().println("""
                    <html><body style="font-family:Arial; padding:20px;">
                      <h2>❌ Delete Failed: %s</h2>
                      <p><a href="listReservations">Back to List</a></p>
                    </body></html>
                    """.formatted(e.getMessage()));
        }
    }
}