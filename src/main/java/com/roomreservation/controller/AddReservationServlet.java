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

        boolean success;
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
            success = true;
            message = "Reservation Added Successfully!";

        } catch (Exception e) {
            success = false;
            message = "Error: " + e.getMessage();
        }

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String title = success ? "Success" : "Error";
        String emoji = success ? "✅" : "❌";
        String boxBg = success ? "#ecfdf5" : "#fef2f2";
        String boxBorder = success ? "#86efac" : "#fecaca";
        String boxText = success ? "#065f46" : "#991b1b";

        resp.getWriter().println("""
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
                    }
                    .card{
                      width:min(700px, 100%%);
                      background:#fff;
                      border-radius:16px;
                      padding:28px;
                      box-shadow:0 18px 55px rgba(0,0,0,0.08);
                      animation: fadeIn .5s ease-in-out;
                      text-align:center;
                    }
                    @keyframes fadeIn{
                      from{opacity:0; transform:translateY(18px);}
                      to{opacity:1; transform:translateY(0);}
                    }
                    h2{
                      color:#1e3a8a;
                      margin-bottom:10px;
                      font-size:24px;
                    }
                    .msg{
                      margin-top:12px;
                      padding:14px;
                      border-radius:14px;
                      background:%s;
                      border:1px solid %s;
                      color:%s;
                      font-weight:700;
                      line-height:1.6;
                    }
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
                  </style>
                </head>

                <body>
                  <div class="card">
                    <h2>%s</h2>

                    <div class="msg">%s %s</div>

                    <div class="actions">
                      <a class="btn btn-primary" href="addReservation.html">Add Another</a>
                      <a class="btn btn-light" href="menu">Back to Menu</a>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                title,
                boxBg, boxBorder, boxText,
                title,
                emoji, message
        ));
    }
}