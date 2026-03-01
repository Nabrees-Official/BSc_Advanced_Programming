package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewReservationServlet extends HttpServlet {

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
                    .title{
                      text-align:center;
                      margin-bottom:16px;
                    }
                    .title h2{
                      color:#1e3a8a;
                      font-size:26px;
                      margin-bottom:6px;
                    }
                    .title p{
                      color:#6b7280;
                      font-size:14px;
                    }

                    table{
                      width:100%%;
                      border-collapse:collapse;
                      margin-top:14px;
                      overflow:hidden;
                      border-radius:14px;
                      border:1px solid #e5e7eb;
                    }
                    th, td{
                      padding:14px 12px;
                      border-bottom:1px solid #eef2ff;
                      text-align:left;
                      font-size:14px;
                    }
                    th{
                      width:260px;
                      background:#f1f5ff;
                      color:#0f172a;
                      font-weight:700;
                    }
                    tr:last-child td, tr:last-child th{border-bottom:none;}

                    .actions{
                      display:flex;
                      gap:10px;
                      justify-content:center;
                      margin-top:18px;
                      flex-wrap:wrap;
                    }
                    .btn{
                      display:inline-block;
                      padding:10px 16px;
                      border-radius:10px;
                      text-decoration:none;
                      font-weight:700;
                      font-size:14px;
                      transition:.25s;
                      border:1px solid transparent;
                    }
                    .btn-primary{
                      background:#2563eb;
                      color:white;
                      box-shadow:0 10px 22px rgba(37,99,235,0.18);
                    }
                    .btn-primary:hover{
                      background:#1e40af;
                      transform:translateY(-2px);
                    }
                    .btn-light{
                      background:#ffffff;
                      color:#2563eb;
                      border-color:#c7d2fe;
                    }
                    .btn-light:hover{
                      background:#f1f5ff;
                      transform:translateY(-2px);
                    }
                    .msg{
                      text-align:center;
                      padding:14px;
                      border-radius:14px;
                      background:#f8fafc;
                      border:1px dashed #cbd5e1;
                      color:#475569;
                      margin-top:12px;
                      font-size:14px;
                      line-height:1.6;
                    }
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");

        // ✅ Validation: empty
        if (idParam == null || idParam.isBlank()) {
            resp.getWriter().println(pageStart("Reservation Details") + """
                    <div class="title">
                      <h2>Reservation Details</h2>
                      <p>Search result</p>
                    </div>

                    <div class="msg">
                      ❌ <b>Reservation ID is required.</b><br>
                      Please enter a valid reservation ID and try again.
                    </div>

                    <div class="actions">
                      <a class="btn btn-primary" href="viewReservation.html">Go Back</a>
                      <a class="btn btn-light" href="menu">Back to Menu</a>
                    </div>
                    """ + pageEnd());
            return;
        }

        int id;
        // ✅ Validation: number
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.getWriter().println(pageStart("Reservation Details") + """
                    <div class="title">
                      <h2>Reservation Details</h2>
                      <p>Search result</p>
                    </div>

                    <div class="msg">
                      ❌ <b>Invalid Reservation ID.</b><br>
                      Reservation ID must be a number (Example: 1).
                    </div>

                    <div class="actions">
                      <a class="btn btn-primary" href="viewReservation.html">Go Back</a>
                      <a class="btn btn-light" href="menu">Back to Menu</a>
                    </div>
                    """ + pageEnd());
            return;
        }

        String sql = "SELECT reservation_id, guest_name, address, contact_no, room_type, check_in, check_out, created_at " +
                "FROM reservations WHERE reservation_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                // ✅ Not found
                if (!rs.next()) {
                    resp.getWriter().println(pageStart("Reservation Details") + ("""
                            <div class="title">
                              <h2>No Reservation Found</h2>
                              <p>Reservation ID: <b>%d</b></p>
                            </div>

                            <div class="msg">
                              ❌ No reservation record exists for this ID.<br>
                              Please check the ID and try again.
                            </div>

                            <div class="actions">
                              <a class="btn btn-primary" href="viewReservation.html">Search Again</a>
                              <a class="btn btn-light" href="menu">Back to Menu</a>
                            </div>
                            """.formatted(id)) + pageEnd());
                    return;
                }

                // ✅ Found
                String guestName = rs.getString("guest_name");
                String address = rs.getString("address");
                String contactNo = rs.getString("contact_no");
                String roomType = rs.getString("room_type");
                String checkIn = rs.getString("check_in");
                String checkOut = rs.getString("check_out");
                String createdAt = rs.getString("created_at");

                resp.getWriter().println(pageStart("Reservation Details") + ("""
                        <div class="title">
                          <h2>Reservation Details ✅</h2>
                          <p>Reservation ID: <b>%d</b></p>
                        </div>

                        <table>
                          <tr><th>Reservation ID</th><td>%d</td></tr>
                          <tr><th>Guest Name</th><td>%s</td></tr>
                          <tr><th>Address</th><td>%s</td></tr>
                          <tr><th>Contact No</th><td>%s</td></tr>
                          <tr><th>Room Type</th><td>%s</td></tr>
                          <tr><th>Check-in</th><td>%s</td></tr>
                          <tr><th>Check-out</th><td>%s</td></tr>
                          <tr><th>Created At</th><td>%s</td></tr>
                        </table>

                        <div class="actions">
                          <a class="btn btn-primary" href="viewReservation.html">Search Another</a>
                          <a class="btn btn-light" href="menu">Back to Menu</a>
                        </div>
                        """.formatted(id, id, guestName, address, contactNo, roomType, checkIn, checkOut, createdAt)) + pageEnd());
            }

        } catch (Exception e) {
            resp.getWriter().println(pageStart("Reservation Details") + ("""
                    <div class="title">
                      <h2>Error</h2>
                      <p>Something went wrong</p>
                    </div>

                    <div class="msg">
                      ❌ <b>Error:</b> %s
                    </div>

                    <div class="actions">
                      <a class="btn btn-primary" href="viewReservation.html">Go Back</a>
                      <a class="btn btn-light" href="menu">Back to Menu</a>
                    </div>
                    """.formatted(e.getMessage())) + pageEnd());
        }
    }
}