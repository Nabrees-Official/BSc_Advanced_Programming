package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditReservationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.getWriter().println(errorPage("Reservation ID is required.", "listReservations"));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.getWriter().println(errorPage("Invalid Reservation ID.", "listReservations"));
            return;
        }

        String sql = "SELECT reservation_id, guest_name, address, contact_no, room_type, check_in, check_out " +
                "FROM reservations WHERE reservation_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.getWriter().println(errorPage("No reservation found for ID: " + id, "listReservations"));
                    return;
                }

                String guestName = esc(rs.getString("guest_name"));
                String address = esc(rs.getString("address"));
                String contactNo = esc(rs.getString("contact_no"));
                String roomType = esc(rs.getString("room_type"));
                String checkIn = esc(rs.getString("check_in"));
                String checkOut = esc(rs.getString("check_out"));

                resp.getWriter().println(pageStart("Edit Reservation") + """
                        <div class="title">
                          <h2>Edit Reservation ✏️</h2>
                          <p>Reservation ID: <b>#%d</b></p>
                        </div>

                        <form method="post" action="editReservation" class="form">
                          <input type="hidden" name="id" value="%d"/>

                          <label>Guest Name</label>
                          <input type="text" name="guest_name" value="%s" required />

                          <label>Address</label>
                          <input type="text" name="address" value="%s" required />

                          <label>Contact No</label>
                          <input type="text" name="contact_no" value="%s" required />

                          <label>Room Type</label>
                          <select name="room_type" required>
                            <option value="">--Select--</option>
                            <option value="Single" %s>Single</option>
                            <option value="Double" %s>Double</option>
                            <option value="Suite"  %s>Suite</option>
                          </select>

                          <label>Check-in Date</label>
                          <input type="date" name="check_in" value="%s" required />

                          <label>Check-out Date</label>
                          <input type="date" name="check_out" value="%s" required />

                          <div class="actions">
                            <button class="btn btn-primary" type="submit">Update Reservation</button>
                            <a class="btn btn-light" href="listReservations">Cancel</a>
                          </div>
                        </form>
                        """.formatted(
                        id, id,
                        guestName, address, contactNo,
                        selected(roomType, "Single"),
                        selected(roomType, "Double"),
                        selected(roomType, "Suite"),
                        checkIn, checkOut
                ) + pageEnd());
            }

        } catch (Exception e) {
            resp.getWriter().println(errorPage("Error: " + e.getMessage(), "listReservations"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        int id = Integer.parseInt(req.getParameter("id"));
        String guestName = req.getParameter("guest_name");
        String address = req.getParameter("address");
        String contactNo = req.getParameter("contact_no");
        String roomType = req.getParameter("room_type");
        String checkIn = req.getParameter("check_in");
        String checkOut = req.getParameter("check_out");

        String sql = "UPDATE reservations SET guest_name=?, address=?, contact_no=?, room_type=?, check_in=?, check_out=? " +
                "WHERE reservation_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, guestName);
            ps.setString(2, address);
            ps.setString(3, contactNo);
            ps.setString(4, roomType);
            ps.setString(5, checkIn);
            ps.setString(6, checkOut);
            ps.setInt(7, id);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                resp.sendRedirect("viewReservation?id=" + id);
            } else {
                resp.getWriter().println(errorPage("Update failed. Reservation not found.", "listReservations"));
            }

        } catch (Exception e) {
            resp.getWriter().println(errorPage("Error: " + e.getMessage(), "listReservations"));
        }
    }

    // ---------- Helpers (same theme) ----------
    private String selected(String current, String value) {
        if (current == null) return "";
        return current.equalsIgnoreCase(value) ? "selected" : "";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    private String errorPage(String msg, String backLink) {
        return pageStart("Error") + """
                <div class="msg error">❌ <b>%s</b></div>
                <div class="actions">
                  <a class="btn btn-primary" href="%s">Go Back</a>
                  <a class="btn btn-light" href="menu">Back to Menu</a>
                </div>
                """.formatted(esc(msg), backLink) + pageEnd();
    }

    private String pageStart(String title) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                  <style>
                    *{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',Arial,sans-serif;}
                    body{
                      min-height:100vh;
                      display:flex;
                      justify-content:center;
                      align-items:center;
                      background: linear-gradient(135deg, #f4f7ff, #e8f0fe);
                      padding:20px;
                      color:#111827;
                    }
                    .card{
                      width:min(900px, 100%%);
                      background:#fff;
                      border-radius:16px;
                      padding:28px;
                      box-shadow:0 18px 55px rgba(0,0,0,0.08);
                      animation: fadeIn .5s ease-in-out;
                    }
                    @keyframes fadeIn{
                      from{opacity:0; transform:translateY(18px);}
                      to{opacity:1; transform:translateY(0);}
                    }
                    .title{text-align:center;margin-bottom:14px;}
                    .title h2{color:#1e3a8a;font-size:26px;margin-bottom:6px;font-weight:800;}
                    .title p{color:#6b7280;font-size:14px;}

                    .form{display:grid; gap:10px; margin-top:10px;}
                    label{font-weight:800; color:#0f172a; font-size:13px;}
                    input, select{
                      width:100%%;
                      padding:12px;
                      border-radius:12px;
                      border:1px solid #e5e7eb;
                      outline:none;
                      font-size:14px;
                    }
                    input:focus, select:focus{border-color:#93c5fd; box-shadow:0 0 0 4px rgba(37,99,235,.12);}

                    .actions{
                      display:flex; gap:10px; justify-content:center; flex-wrap:wrap;
                      margin-top:14px;
                    }
                    .btn{
                      display:inline-block;
                      padding:10px 16px;
                      border-radius:10px;
                      text-decoration:none;
                      font-weight:800;
                      font-size:14px;
                      transition:.25s;
                      border:1px solid transparent;
                      cursor:pointer;
                    }
                    .btn-primary{background:#2563eb;color:#fff;box-shadow:0 10px 22px rgba(37,99,235,0.18);}
                    .btn-primary:hover{background:#1e40af; transform:translateY(-2px);}
                    .btn-light{background:#fff;color:#2563eb;border-color:#c7d2fe;}
                    .btn-light:hover{background:#f1f5ff; transform:translateY(-2px);}

                    .msg{padding:16px;border-radius:14px;text-align:center;line-height:1.6;font-size:14px;}
                    .msg.error{border:1px solid #fecaca;background:#fef2f2;color:#991b1b;font-weight:800;}
                  </style>
                </head>
                <body>
                  <div class="card">
                """.formatted(title);
    }

    private String pageEnd() {
        return """
                  </div>
                </body>
                </html>
                """;
    }
}