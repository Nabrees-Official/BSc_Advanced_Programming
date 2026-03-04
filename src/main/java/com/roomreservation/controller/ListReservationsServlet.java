package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ListReservationsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        // -------------------------
        // 1) Read filters (GET params)
        // -------------------------
        String idParam = trim(req.getParameter("id"));          // quick ID
        String guest = trim(req.getParameter("guest"));         // guest name search
        String room = trim(req.getParameter("room"));           // Single/Double/Suite
        String from = trim(req.getParameter("from"));           // check_in >= from
        String to = trim(req.getParameter("to"));               // check_out <= to
        String sort = trim(req.getParameter("sort"));           // newest/oldest/guest_az/room

        // Normalize room / sort
        if (!isOneOf(room, "Single", "Double", "Suite")) room = "";
        if (!isOneOf(sort, "newest", "oldest", "guest_az", "room")) sort = "newest";

        // -------------------------
        // 2) Build SQL dynamically + params
        // -------------------------
        StringBuilder sql = new StringBuilder(
                "SELECT reservation_id, guest_name, room_type, check_in, check_out, created_at " +
                        "FROM reservations WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Quick ID filter (exact match)
        if (!idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                sql.append(" AND reservation_id = ? ");
                params.add(id);
            } catch (NumberFormatException ignored) {
                // If invalid ID typed, no results (safe behavior)
                sql.append(" AND 1=0 ");
            }
        }

        // Guest name LIKE
        if (!guest.isEmpty()) {
            sql.append(" AND guest_name LIKE ? ");
            params.add("%" + guest + "%");
        }

        // Room type exact
        if (!room.isEmpty()) {
            sql.append(" AND room_type = ? ");
            params.add(room);
        }

        // Date range
        if (!from.isEmpty()) {
            sql.append(" AND check_in >= ? ");
            params.add(from); // yyyy-mm-dd (MySQL DATE)
        }
        if (!to.isEmpty()) {
            sql.append(" AND check_out <= ? ");
            params.add(to);
        }

        // Sort
        switch (sort) {
            case "oldest" -> sql.append(" ORDER BY created_at ASC ");
            case "guest_az" -> sql.append(" ORDER BY guest_name ASC ");
            case "room" -> sql.append(" ORDER BY room_type ASC, created_at DESC ");
            default -> sql.append(" ORDER BY created_at DESC "); // newest
        }

        // -------------------------
        // 3) Query + build table rows
        // -------------------------
        StringBuilder rows = new StringBuilder();
        boolean hasError = false;
        String errorMsg = "";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("reservation_id");
                    String g = safe(rs.getString("guest_name"));
                    String r = safe(rs.getString("room_type"));
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
                                <a class="btn btn-edit" href="editReservation?id=%d">Edit</a>

                                <form class="inlineForm" method="post" action="deleteReservation"
                                      onsubmit="return confirm('Are you sure you want to delete reservation #%d ?');">
                                  <input type="hidden" name="id" value="%d">
                                  <button class="btn btn-delete" type="submit">Delete</button>
                                </form>
                              </td>
                            </tr>
                            """.formatted(id, g, r, checkIn, checkOut, created, id, id, id, id));
                }
            }

        } catch (Exception e) {
            hasError = true;
            errorMsg = e.getMessage();
        }

        // -------------------------
        // 4) Build filter form (keep values)
        // -------------------------
        String filterForm = """
                <form class="filters" method="get" action="listReservations">
                  <div class="grid">

                    <div class="field">
                      <label>Reservation ID</label>
                      <input type="number" name="id" placeholder="e.g. 12" value="%s">
                    </div>

                    <div class="field">
                      <label>Guest Name</label>
                      <input type="text" name="guest" placeholder="e.g. Kamal" value="%s">
                    </div>

                    <div class="field">
                      <label>Room Type</label>
                      <select name="room">
                        <option value="">All</option>
                        <option value="Single" %s>Single</option>
                        <option value="Double" %s>Double</option>
                        <option value="Suite"  %s>Suite</option>
                      </select>
                    </div>

                    <div class="field">
                      <label>Check-in From</label>
                      <input type="date" name="from" value="%s">
                    </div>

                    <div class="field">
                      <label>Check-out To</label>
                      <input type="date" name="to" value="%s">
                    </div>

                    <div class="field">
                      <label>Sort By</label>
                      <select name="sort">
                        <option value="newest"  %s>Newest First</option>
                        <option value="oldest"  %s>Oldest First</option>
                        <option value="guest_az"%s>Guest Name (A-Z)</option>
                        <option value="room"    %s>Room Type</option>
                      </select>
                    </div>

                  </div>

                  <div class="filterActions">
                    <button class="btn btn-primary" type="submit">Apply Filters</button>
                    <a class="btn btn-light" href="listReservations">Clear</a>
                  </div>
                </form>
                """.formatted(
                escAttr(idParam),
                escAttr(guest),
                selected(room, "Single"),
                selected(room, "Double"),
                selected(room, "Suite"),
                escAttr(from),
                escAttr(to),
                selected(sort, "newest"),
                selected(sort, "oldest"),
                selected(sort, "guest_az"),
                selected(sort, "room")
        );

        // -------------------------
        // 5) Content blocks
        // -------------------------
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
                      No reservations match your filters.<br>
                      Try adjusting filters or click <b>Clear</b>.
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
                            <th style="text-align:center;">Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                      </table>
                    </div>
                    """.formatted(rows.toString());
        }

        // -------------------------
        // 6) Render page
        // -------------------------
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

                    /* ---------- Filters UI ---------- */
                    .filters{
                      border:1px solid #e5e7eb;
                      border-radius:14px;
                      padding:14px;
                      background:#fbfdff;
                      margin-bottom:14px;
                    }
                    .grid{
                      display:grid;
                      grid-template-columns: repeat(3, 1fr);
                      gap:12px;
                    }
                    .field label{
                      display:block;
                      font-weight:800;
                      font-size:12px;
                      color:#0f172a;
                      margin-bottom:6px;
                    }
                    .field input, .field select{
                      width:100%%;
                      padding:11px 12px;
                      border-radius:12px;
                      border:1px solid #e5e7eb;
                      font-size:14px;
                      outline:none;
                      background:white;
                    }
                    .field input:focus, .field select:focus{
                      border-color:#93c5fd;
                      box-shadow:0 0 0 4px rgba(37,99,235,.12);
                    }

                    .filterActions{
                      margin-top:12px;
                      display:flex;
                      gap:10px;
                      justify-content:center;
                      flex-wrap:wrap;
                    }

                    @media (max-width: 920px){
                      .grid{grid-template-columns: repeat(2, 1fr);}
                    }
                    @media (max-width: 560px){
                      .grid{grid-template-columns: 1fr;}
                    }

                    /* ---------- Table ---------- */
                    .tableWrap{
                      width:100%%;
                      overflow:auto;
                      border-radius: 14px;
                      border: 1px solid #e5e7eb;
                    }

                    table{
                      width:100%%;
                      border-collapse: collapse;
                      min-width: 1050px;
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

                    tbody tr:hover{ background: #f8fafc; }

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

                    .inlineForm{
                      display:inline-block;
                      margin-left:8px;
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
                      cursor:pointer;
                    }

                    .btn-view{
                      background: #2563eb;
                      color:white;
                      box-shadow: 0 10px 22px rgba(37,99,235,0.16);
                    }
                    .btn-view:hover{ background:#1e40af; transform: translateY(-2px); }

                    .btn-edit{
                      background:#ffffff;
                      color:#2563eb;
                      border-color:#c7d2fe;
                    }
                    .btn-edit:hover{ background:#f1f5ff; transform: translateY(-2px); }

                    .btn-delete{
                      background:#ef4444;
                      color:#fff;
                      box-shadow: 0 10px 22px rgba(239,68,68,0.18);
                    }
                    .btn-delete:hover{ background:#b91c1c; transform: translateY(-2px); }

                    .footerActions{
                      margin-top: 14px;
                      display:flex;
                      gap: 10px;
                      justify-content:center;
                      flex-wrap: wrap;
                    }

                    .btn-primary{
                      background:#2563eb;
                      color:#fff;
                    }
                    .btn-primary:hover{ background:#1e40af; transform: translateY(-2px); }

                    .btn-light{
                      background:#ffffff;
                      color:#2563eb;
                      border-color:#c7d2fe;
                    }
                    .btn-light:hover{ background:#f1f5ff; transform: translateY(-2px); }

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
                      <p>Search, filter, sort, and manage reservations.</p>
                    </div>

                    <div class="card">
                      %s
                      %s

                      <div class="footerActions">
                        <a class="btn btn-primary" href="addReservation.html">Add Reservation</a>
                        <a class="btn btn-light" href="menu">Back to Menu</a>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(filterForm, bodyContent));
    }

    // -------------------------
    // Helpers
    // -------------------------
    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private boolean isOneOf(String val, String... allowed) {
        if (val == null) return false;
        for (String a : allowed) {
            if (val.equals(a)) return true;
        }
        return false;
    }

    private String selected(String current, String value) {
        return current != null && current.equals(value) ? "selected" : "";
    }

    private String safe(String s) { return (s == null) ? "" : escapeHtml(s); }

    private String escAttr(String s) {
        // safe for putting inside value="..."
        return escapeHtml(s);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}