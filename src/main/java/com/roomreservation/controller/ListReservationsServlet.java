package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ListReservationsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String sql = "SELECT reservation_id, guest_name, room_type, check_in, check_out, created_at " +
                "FROM reservations ORDER BY reservation_id DESC";

        StringBuilder rows = new StringBuilder();
        boolean hasError = false;
        String errorMsg = "";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("reservation_id");
                String guest = safe(rs.getString("guest_name"));
                String room = safe(rs.getString("room_type"));
                String checkIn = safe(rs.getString("check_in"));
                String checkOut = safe(rs.getString("check_out"));
                String created = safe(rs.getString("created_at"));

                rows.append("""
                        <tr>
                          <td class="mono">#%d</td>
                          <td>%s</td>
                          <td><span class="pill">%s</span></td>
                          <td>%s</td>
                          <td>%s</td>
                          <td class="muted">%s</td>
                          <td class="actionsCol">
                            <a class="btn btn-view" href="viewReservation?id=%d">View</a>
                          </td>
                        </tr>
                        """.formatted(id, guest, room, checkIn, checkOut, created, id));
            }

        } catch (Exception e) {
            hasError = true;
            errorMsg = e.getMessage();
        }

        String bodyContent;
        if (hasError) {
            bodyContent = """
                    <div class="msg error">
                      ❌ <b>Error:</b> %s
                    </div>
                    """.formatted(escapeHtml(errorMsg));
        } else if (rows.length() == 0) {
            bodyContent = """
                    <div class="msg">
                      No reservations found yet.<br>
                      Click <b>Add Reservation</b> to create the first booking.
                    </div>
                    """;
        } else {
            bodyContent = """
                    <div class="tableWrap">
                      <table>
                        <thead>
                          <tr>
                            <th>ID</th>
                            <th>Guest</th>
                            <th>Room Type</th>
                            <th>Check-in</th>
                            <th>Check-out</th>
                            <th>Created At</th>
                            <th style="text-align:center;">Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                      </table>
                    </div>
                    """.formatted(rows.toString());
        }

        resp.getWriter().println("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>All Reservations</title>

                  <style>
                    *{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',Arial,sans-serif;}
                    body{
                      min-height:100vh;
                      background: linear-gradient(135deg, #f4f7ff, #e8f0fe);
                      padding: 26px 16px;
                      color:#111827;
                    }

                    .container{
                      width:min(1100px, 100%%);
                      margin:0 auto;
                      animation: fadeIn .5s ease-in-out;
                    }
                    @keyframes fadeIn{
                      from{opacity:0; transform: translateY(18px);}
                      to{opacity:1; transform: translateY(0);}
                    }

                    .header{
                      background:#fff;
                      padding: 22px 22px;
                      border-radius: 16px;
                      box-shadow: 0 18px 55px rgba(0,0,0,0.08);
                      margin-bottom: 16px;
                      text-align:center;
                    }
                    .header h2{
                      color:#1e3a8a;
                      font-size: 26px;
                      margin-bottom: 6px;
                      font-weight: 800;
                    }
                    .header p{
                      color:#6b7280;
                      font-size: 14px;
                      line-height: 1.5;
                    }

                    .card{
                      background:#fff;
                      border-radius: 16px;
                      box-shadow: 0 18px 55px rgba(0,0,0,0.08);
                      padding: 18px;
                    }

                    .tableWrap{
                      width:100%%;
                      overflow:auto; /* ✅ mobile scroll */
                      border-radius: 14px;
                      border: 1px solid #e5e7eb;
                    }

                    table{
                      width:100%%;
                      border-collapse: collapse;
                      min-width: 900px; /* ✅ keeps columns readable */
                      background: #fff;
                    }

                    thead th{
                      position: sticky;
                      top: 0;
                      background: #f1f5ff;
                      color: #0f172a;
                      text-align: left;
                      font-size: 13px;
                      letter-spacing: .2px;
                      padding: 12px 12px;
                      border-bottom: 1px solid #e5e7eb;
                      z-index: 1;
                    }

                    tbody td{
                      padding: 12px 12px;
                      border-bottom: 1px solid #eef2ff;
                      font-size: 14px;
                      vertical-align: middle;
                    }

                    tbody tr:hover{
                      background: #f8fafc;
                    }

                    .mono{
                      font-variant-numeric: tabular-nums;
                      font-weight: 700;
                      color:#111827;
                      white-space: nowrap;
                    }

                    .muted{
                      color:#6b7280;
                      font-size: 13px;
                      white-space: nowrap;
                    }

                    .pill{
                      display:inline-block;
                      padding: 6px 10px;
                      border-radius: 999px;
                      background: rgba(37,99,235,0.10);
                      border: 1px solid rgba(37,99,235,0.18);
                      color: #2563eb;
                      font-weight: 700;
                      font-size: 12px;
                      white-space: nowrap;
                    }

                    .actionsCol{
                      text-align:center;
                      white-space: nowrap;
                    }

                    .btn{
                      display:inline-block;
                      padding: 8px 12px;
                      border-radius: 10px;
                      text-decoration:none;
                      font-weight: 800;
                      font-size: 13px;
                      transition: .2s ease;
                      border: 1px solid transparent;
                    }

                    .btn-view{
                      background: #2563eb;
                      color:white;
                      box-shadow: 0 10px 22px rgba(37,99,235,0.16);
                    }
                    .btn-view:hover{
                      background: #1e40af;
                      transform: translateY(-2px);
                    }

                    .footerActions{
                      margin-top: 14px;
                      display:flex;
                      gap: 10px;
                      justify-content:center;
                      flex-wrap: wrap;
                    }

                    .btn-light{
                      background:#ffffff;
                      color:#2563eb;
                      border-color:#c7d2fe;
                    }
                    .btn-light:hover{
                      background:#f1f5ff;
                      transform: translateY(-2px);
                    }

                    .btn-primary{
                      background:#2563eb;
                      color:#fff;
                    }
                    .btn-primary:hover{
                      background:#1e40af;
                      transform: translateY(-2px);
                    }

                    .msg{
                      padding: 16px;
                      border-radius: 14px;
                      border: 1px dashed #cbd5e1;
                      background: #f8fafc;
                      color:#475569;
                      text-align:center;
                      line-height: 1.6;
                      font-size: 14px;
                    }
                    .msg.error{
                      border: 1px solid #fecaca;
                      background:#fef2f2;
                      color:#991b1b;
                    }
                  </style>
                </head>

                <body>
                  <div class="container">

                    <div class="header">
                      <h2>All Reservations</h2>
                      <p>View, manage, and quickly open reservation records.</p>
                    </div>

                    <div class="card">
                      %s

                      <div class="footerActions">
                        <a class="btn btn-primary" href="addReservation.html">Add Reservation</a>
                        <a class="btn btn-light" href="menu">Back to Menu</a>
                      </div>
                    </div>

                  </div>
                </body>
                </html>
                """.formatted(bodyContent));
    }

    // Avoid null text
    private String safe(String s) {
        return (s == null) ? "" : escapeHtml(s);
    }

    // Basic HTML escape for safety
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}