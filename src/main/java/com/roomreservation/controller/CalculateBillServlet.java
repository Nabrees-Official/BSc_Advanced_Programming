package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalculateBillServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");

        // ✅ Validate: empty
        if (idParam == null || idParam.isBlank()) {
            resp.getWriter().println(renderError("Reservation ID is required.", "calculateBill.html"));
            return;
        }

        int id;
        // ✅ Validate: number
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.getWriter().println(renderError("Invalid Reservation ID. Please enter a number (Example: 1).", "calculateBill.html"));
            return;
        }

        String sqlReservation =
                "SELECT reservation_id, guest_name, room_type, check_in, check_out " +
                        "FROM reservations WHERE reservation_id = ?";

        String sqlRate =
                "SELECT rate_per_night FROM room_rates WHERE room_type = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement psRes = con.prepareStatement(sqlReservation)) {

            psRes.setInt(1, id);

            try (ResultSet rs = psRes.executeQuery()) {

                // ✅ Not found
                if (!rs.next()) {
                    resp.getWriter().println(renderError("No reservation found for ID: " + id, "calculateBill.html"));
                    return;
                }

                String guestName = rs.getString("guest_name");
                String roomType = rs.getString("room_type");
                LocalDate checkIn = rs.getDate("check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out").toLocalDate();

                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (nights <= 0) nights = 1; // safety

                BigDecimal rate;

                try (PreparedStatement psRate = con.prepareStatement(sqlRate)) {
                    psRate.setString(1, roomType);

                    try (ResultSet rr = psRate.executeQuery()) {
                        if (!rr.next()) {
                            resp.getWriter().println(renderError("No rate found for room type: " + roomType, "calculateBill.html"));
                            return;
                        }
                        rate = rr.getBigDecimal("rate_per_night");
                    }
                }

                BigDecimal total = rate.multiply(BigDecimal.valueOf(nights));

                resp.getWriter().println(renderBillPage(id, guestName, roomType, checkIn, checkOut, nights, rate, total));
            }

        } catch (Exception e) {
            resp.getWriter().println(renderError("Error: " + e.getMessage(), "calculateBill.html"));
        }
    }

    // ✅ Success page (Bill details) + ✅ Print Button
    private String renderBillPage(int id, String guestName, String roomType,
                                  LocalDate checkIn, LocalDate checkOut,
                                  long nights, BigDecimal rate, BigDecimal total) {

        return pageStart("Bill Details") + """
                <div class="title">
                    <h2>Bill Details ✅</h2>
                    <p>Reservation ID: <b>%d</b></p>
                </div>

                <div id="printArea">
                    <table>
                        <tr><th>Reservation ID</th><td>%d</td></tr>
                        <tr><th>Guest Name</th><td>%s</td></tr>
                        <tr><th>Room Type</th><td>%s</td></tr>
                        <tr><th>Check-in</th><td>%s</td></tr>
                        <tr><th>Check-out</th><td>%s</td></tr>
                        <tr><th>Nights</th><td>%d</td></tr>
                        <tr><th>Rate per night (LKR)</th><td>%s</td></tr>
                        <tr><th>Total (LKR)</th><td><b>%s</b></td></tr>
                    </table>
                </div>

                <div class="actions no-print">
                    <button class="btn btn-print" onclick="printBill()">Print Bill</button>
                    <a class="btn btn-primary" href="calculateBill.html">Calculate Another</a>
                    <a class="btn btn-light" href="menu">Back to Menu</a>
                </div>

                <script>
                    function printBill(){
                        window.print();
                    }
                </script>
                """.formatted(id, id, guestName, roomType, checkIn, checkOut, nights, rate, total)
                + pageEnd();
    }

    // ✅ Error page (same theme)
    private String renderError(String message, String backLink) {
        return pageStart("Calculate Bill") + """
                <div class="title">
                    <h2>Calculate Bill</h2>
                    <p>Result</p>
                </div>

                <div class="msg">
                    ❌ <b>%s</b>
                </div>

                <div class="actions">
                    <a class="btn btn-primary" href="%s">Go Back</a>
                    <a class="btn btn-light" href="menu">Back to Menu</a>
                </div>
                """.formatted(message, backLink)
                + pageEnd();
    }

    // ✅ Common Page Layout (same theme)
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

                    .msg{
                      text-align:center;
                      padding:14px;
                      border-radius:14px;
                      background:#fef2f2;
                      border:1px solid #fecaca;
                      color:#991b1b;
                      margin-top:12px;
                      font-size:14px;
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
                      cursor:pointer;
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

                    /* ✅ Print button */
                    .btn-print{
                      background:#10b981;
                      color:white;
                      box-shadow:0 10px 22px rgba(16,185,129,0.18);
                    }
                    .btn-print:hover{
                      background:#059669;
                      transform:translateY(-2px);
                    }

                    /* ✅ Hide buttons when printing */
                    @media print{
                      .no-print{display:none;}
                      body{background:white;}
                      .card{box-shadow:none;border:none;}
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
}